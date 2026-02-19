package pl.pointblank.planszowsky.data.repository

import pl.pointblank.planszowsky.data.local.GameDao
import pl.pointblank.planszowsky.data.local.toDomainModel
import pl.pointblank.planszowsky.data.local.toEntity
import pl.pointblank.planszowsky.data.remote.BggApi
import pl.pointblank.planszowsky.domain.model.Game
import pl.pointblank.planszowsky.domain.model.Expansion
import pl.pointblank.planszowsky.domain.repository.GameRepository
import pl.pointblank.planszowsky.util.FirebaseManager
import pl.pointblank.planszowsky.util.decodeHtml
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import pl.pointblank.planszowsky.util.similarity
import retrofit2.HttpException
import javax.inject.Inject

import kotlinx.coroutines.flow.combine
import pl.pointblank.planszowsky.domain.model.CollectionStats

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class GameRepositoryImpl @Inject constructor(
    private val dao: GameDao,
    private val api: BggApi,
    private val okHttpClient: OkHttpClient,
    private val firebaseManager: FirebaseManager
) : GameRepository {

    private val mapper = jacksonObjectMapper()

    override fun getCollectionStats(collectionId: String): Flow<CollectionStats> {
        return combine(
            dao.getOwnedCount(collectionId),
            dao.getWishlistCount(collectionId),
            dao.getFavoriteCount(collectionId),
            dao.getLentCount(collectionId),
            dao.getAllOwnedCategories(collectionId)
        ) { owned, wishlist, favorite, lent, categoriesRaw ->
            val categoryMap = mapOf(
                "Card Game" to "Karciarz",
                "Fantasy" to "Fan Fantasy",
                "Economic" to "Strateg / Ekonomista",
                "War Game" to "Wojownik",
                "Adventure" to "Poszukiwacz Przygód",
                "Dice" to "Fan Kości",
                "Party Game" to "Król Imprezy",
                "Abstract Strategy" to "Abstrakcyjny Umysł",
                "Horror" to "Fan Grozy",
                "Science Fiction" to "Fan Sci-Fi",
                "Medieval" to "Władca Średniowiecza",
                "Civilization" to "Budowniczy Cywilizacji",
                "Exploration" to "Odkrywca",
                "Ancient" to "Antyczny Badacz"
            )

            val rawTop = categoriesRaw
                .flatMap { it.split(",").filter { cat -> cat.isNotBlank() } }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key

            val topCategory = categoryMap[rawTop] ?: rawTop

            CollectionStats(
                totalOwned = owned,
                wishlistCount = wishlist,
                favoriteCount = favorite,
                lentCount = lent,
                topCategory = topCategory
            )
        }
    }

    private val sessionMutex = Mutex()
    private var isSessionInitialized = false

    private suspend fun ensureSession() {
        if (isSessionInitialized) return
        
        sessionMutex.withLock {
            if (isSessionInitialized) return@withLock
            
            withContext(Dispatchers.IO) {
                try {
                    val request = Request.Builder()
                        .url("https://boardgamegeek.com/")
                        .build()
                    
                    okHttpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            isSessionInitialized = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getSavedGames(collectionId: String): Flow<List<Game>> {
        return combine(dao.getAllGames(collectionId), dao.getAllCollections()) { entities, collections ->
            val isReadOnly = collections.find { it.id == collectionId }?.isReadOnly ?: false
            entities.map { it.toDomainModel().copy(isReadOnly = isReadOnly) }
        }
    }

    override fun getWishlistedGames(collectionId: String): Flow<List<Game>> {
        return combine(dao.getWishlistedGames(collectionId), dao.getAllCollections()) { entities, collections ->
            val isReadOnly = collections.find { it.id == collectionId }?.isReadOnly ?: false
            entities.map { it.toDomainModel().copy(isReadOnly = isReadOnly) }
        }
    }

    override suspend fun getGame(id: String, collectionId: String): Game? {
        val game = dao.getGameById(id, collectionId)?.toDomainModel()
        val collection = dao.getCollectionById(collectionId)
        return game?.copy(isReadOnly = collection?.isReadOnly ?: false)
    }

    override suspend fun saveGame(game: Game) {
        dao.insertGame(game.copy(isOwned = true).toEntity())
    }

    override suspend fun updateGame(game: Game) {
        dao.insertGame(game.toEntity())
    }

    override suspend fun toggleWishlist(game: Game) {
        val nextWishlistState = !game.isWishlisted
        dao.insertGame(game.copy(
            isWishlisted = nextWishlistState,
            isOwned = !nextWishlistState
        ).toEntity())
    }

    override suspend fun toggleFavorite(game: Game) {
        dao.insertGame(game.copy(isFavorite = !game.isFavorite).toEntity())
    }

    override suspend fun deleteGame(game: Game) {
        pl.pointblank.planszowsky.util.ImageManager.deleteImage(game.localImageUri)
        dao.deleteGame(game.toEntity())
    }

    override suspend fun searchRemoteGames(query: String): List<Game> {
        ensureSession()
        return try {
            val searchResponse = api.searchGames(query)
            val ids = searchResponse.items?.map { it.id }?.take(20) ?: emptyList()
            
            if (ids.isEmpty()) return emptyList()
            
            val detailsResponse = api.getGameDetails(ids.joinToString(","))
            detailsResponse.items?.map { item ->
                val bestName = item.names?.map { it.value }?.maxByOrNull { it.similarity(query) }
                    ?: item.names?.find { it.type == "primary" }?.value 
                    ?: item.names?.firstOrNull()?.value 
                    ?: "Unknown"
                
                Game(
                    id = item.id,
                    title = bestName.decodeHtml(),
                    thumbnailUrl = item.thumbnail,
                    imageUrl = item.image,
                    yearPublished = item.yearPublished?.value,
                    websiteUrl = item.links?.find { it.type == "boardgamewebsite" }?.value
                )
            } ?: emptyList()
        } catch (e: HttpException) {
            firebaseManager.logError(e, "BGG Search Error (${e.code()}): $query")
            throw e
        } catch (e: Exception) {
            firebaseManager.logError(e, "BGG Search Exception: $query")
            throw e
        }
    }

    override suspend fun searchByBarcode(barcode: String): List<Game> {
        ensureSession()
        return try {
            val searchResponse = api.searchByBarcode(barcode)
            val ids = searchResponse.items?.map { it.id }?.take(20) ?: emptyList()
            
            if (ids.isEmpty()) return emptyList()
            
            val detailsResponse = api.getGameDetails(ids.joinToString(","))
            detailsResponse.items?.map { item ->
                val primaryName = item.names?.find { it.type == "primary" }?.value 
                    ?: item.names?.firstOrNull()?.value 
                    ?: "Unknown"
                
                Game(
                    id = item.id,
                    title = primaryName.decodeHtml(),
                    thumbnailUrl = item.thumbnail,
                    imageUrl = item.image,
                    yearPublished = item.yearPublished?.value,
                    websiteUrl = item.links?.find { it.type == "boardgamewebsite" }?.value
                )
            } ?: emptyList()
        } catch (e: HttpException) {
            firebaseManager.logError(e, "BGG Barcode Error (${e.code()}): $barcode")
            throw e
        } catch (e: Exception) {
            firebaseManager.logError(e, "BGG Barcode Exception: $barcode")
            throw e
        }
    }

    override suspend fun getRemoteGameDetails(id: String, preferredTitle: String?): Game? {
        ensureSession()
        return try {
            val response = api.getGameDetails(id)
            val item = response.items?.firstOrNull() ?: return null
            
            val title = preferredTitle ?: item.names?.find { it.type == "primary" }?.value 
                ?: item.names?.firstOrNull()?.value 
                ?: "Unknown"

            val categories = item.links?.filter { it.type == "boardgamecategory" }?.map { it.value } ?: emptyList()
            
            val expansions = item.links
                ?.filter { it.type == "boardgameexpansion" && it.inbound != "true" }
                ?.map { Expansion(id = it.id, title = it.value, isOwned = false) }
                ?: emptyList()

            Game(
                id = item.id,
                title = title.decodeHtml(),
                thumbnailUrl = item.thumbnail,
                imageUrl = item.image,
                description = item.description?.decodeHtml(),
                yearPublished = item.yearPublished?.value,
                minPlayers = item.minPlayers?.value,
                maxPlayers = item.maxPlayers?.value,
                playingTime = item.playingTime?.value,
                isOwned = false,
                categories = categories,
                expansions = expansions,
                websiteUrl = item.links?.find { it.type == "boardgamewebsite" }?.value
            )
        } catch (e: HttpException) {
            firebaseManager.logError(e, "BGG Details Error (${e.code()}): $id")
            null
        } catch (e: Exception) {
            firebaseManager.logError(e, "BGG Details Exception: $id")
            null
        }
    }


    override suspend fun updateNotes(gameId: String, collectionId: String, notes: String) {
        val game = getGame(gameId, collectionId)
        game?.let {
            dao.insertGame(it.copy(notes = notes).toEntity())
        }
    }

    override suspend fun updateLocalImage(gameId: String, collectionId: String, imagePath: String?) {
        val game = getGame(gameId, collectionId)
        game?.let {
            // Delete old image if exists
            if (it.localImageUri != imagePath) {
                pl.pointblank.planszowsky.util.ImageManager.deleteImage(it.localImageUri)
            }
            dao.insertGame(it.copy(localImageUri = imagePath).toEntity())
        }
    }

    override suspend fun fetchCollection(username: String): List<Game> {
        ensureSession()
        val allGames = mutableListOf<Game>()
        try {
            val response = api.getCollection(username)
            val items = response.items ?: emptyList()
            if (items.isEmpty()) return emptyList()

            items.chunked(20).forEach { batch ->
                val ids = batch.joinToString(",") { it.id }
                try {
                    val detailsResponse = api.getGameDetails(ids)
                    val detailsMap = detailsResponse.items?.associateBy { it.id } ?: emptyMap()

                    batch.forEach { item ->
                        val details = detailsMap[item.id]
                        val game = Game(
                            id = item.id,
                            title = item.name?.decodeHtml() ?: details?.names?.find { it.type == "primary" }?.value ?: "Unknown",
                            thumbnailUrl = item.thumbnail,
                            imageUrl = item.image,
                            description = details?.description?.decodeHtml(),
                            yearPublished = item.yearPublished,
                            minPlayers = item.stats?.minplayers ?: details?.minPlayers?.value,
                            maxPlayers = item.stats?.maxplayers ?: details?.maxPlayers?.value,
                            playingTime = item.stats?.playingtime ?: details?.playingTime?.value,
                            notes = item.comment,
                            isOwned = item.status?.own == "1",
                            isWishlisted = item.status?.wishlist == "1",
                            categories = details?.links?.filter { it.type == "boardgamecategory" }?.map { it.value } ?: emptyList(),
                            collectionId = "main",
                            websiteUrl = details?.links?.find { it.type == "boardgamewebsite" }?.value
                        )
                        allGames.add(game)
                    }
                } catch (e: Exception) {
                    batch.forEach { item ->
                        allGames.add(Game(id = item.id, title = item.name?.decodeHtml() ?: "Unknown", isOwned = item.status?.own == "1", collectionId = "main"))
                    }
                }
            }

            return mergeExpansionsIntelligently(allGames)

        } catch (e: Exception) {
            firebaseManager.logError(e, "BGG Fetch Exception: $username")
            throw e
        }
    }

    private suspend fun mergeExpansionsIntelligently(games: List<Game>): List<Game> {
        val gameMap = games.associateBy { it.id }.toMutableMap()
        val handledAsExpansion = mutableSetOf<String>()
        val childToParent = mutableMapOf<String, String>()

        try {
            games.chunked(20).forEach { batch ->
                val details = api.getGameDetails(batch.joinToString(",") { it.id })
                details.items?.forEach { item ->
                    val currentGame = gameMap[item.id] ?: return@forEach
                    
                    val allKnown = item.links
                        ?.filter { it.type == "boardgameexpansion" && it.inbound != "true" }
                        ?.map { Expansion(id = it.id, title = it.value, isOwned = false) }
                        ?: emptyList()
                    
                    gameMap[item.id] = currentGame.copy(expansions = allKnown)

                    item.links?.find { it.type == "boardgameexpansion" && it.inbound == "true" }?.id?.let { parentId ->
                        childToParent[item.id] = parentId
                    }
                }
            }

            gameMap.values.forEach { game ->
                val parentId = childToParent[game.id]
                if (parentId != null && gameMap.containsKey(parentId)) {
                    val parent = gameMap[parentId]!!
                    val updatedExp = parent.expansions.map { 
                        if (it.id == game.id) it.copy(isOwned = true) else it
                    }.toMutableList()
                    if (updatedExp.none { it.id == game.id }) {
                        updatedExp.add(Expansion(id = game.id, title = game.title, isOwned = true))
                    }
                    gameMap[parentId] = parent.copy(expansions = updatedExp)
                    handledAsExpansion.add(game.id)
                }
            }

            return gameMap.filter { !handledAsExpansion.contains(it.key) }.values.toList()
        } catch (e: Exception) {
            return games
        }
    }

    override suspend fun saveImportedGames(games: List<Game>, overwriteExisting: Boolean, collectionId: String): Int {
        var count = 0
        val incompleteGames = games.filter { it.imageUrl.isNullOrBlank() || it.description.isNullOrBlank() }
        val enrichedData = mutableMapOf<String, Game>()
        
        if (incompleteGames.isNotEmpty()) {
            incompleteGames.chunked(20).forEach { batch ->
                try {
                    val ids = batch.joinToString(",") { it.id }
                    val detailsResponse = api.getGameDetails(ids)
                    detailsResponse.items?.forEach { item ->
                        enrichedData[item.id] = Game(
                            id = item.id,
                            title = item.names?.find { it.type == "primary" }?.value ?: "",
                            thumbnailUrl = item.thumbnail,
                            imageUrl = item.image,
                            description = item.description?.decodeHtml(),
                            yearPublished = item.yearPublished?.value,
                            minPlayers = item.minPlayers?.value,
                            maxPlayers = item.maxPlayers?.value,
                            playingTime = item.playingTime?.value,
                            categories = item.links?.filter { it.type == "boardgamecategory" }?.map { it.value } ?: emptyList(),
                            expansions = item.links?.filter { it.type == "boardgameexpansion" && it.inbound != "true" }?.map { Expansion(id = it.id, title = it.value, isOwned = false) } ?: emptyList()
                        )
                    }
                } catch (e: Exception) {
                    firebaseManager.logError(e, "Enrichment Error during import")
                }
            }
        }

        games.forEach { game ->
            try {
                val existing = dao.getGameById(game.id, collectionId)
                if (existing == null || overwriteExisting) {
                    val enriched = enrichedData[game.id]
                    val gameToSave = game.copy(
                        collectionId = collectionId,
                        thumbnailUrl = game.thumbnailUrl.takeIf { !it.isNullOrBlank() } ?: enriched?.thumbnailUrl,
                        imageUrl = game.imageUrl.takeIf { !it.isNullOrBlank() } ?: enriched?.imageUrl,
                        description = game.description.takeIf { !it.isNullOrBlank() } ?: enriched?.description,
                        yearPublished = game.yearPublished ?: enriched?.yearPublished,
                        minPlayers = game.minPlayers ?: enriched?.minPlayers,
                        maxPlayers = game.maxPlayers ?: enriched?.maxPlayers,
                        playingTime = game.playingTime ?: enriched?.playingTime,
                        categories = game.categories.takeIf { it.isNotEmpty() } ?: enriched?.categories ?: emptyList(),
                        expansions = if (game.expansions.isNotEmpty()) {
                            val known = enriched?.expansions ?: emptyList()
                            if (known.isEmpty()) game.expansions else {
                                known.map { k -> 
                                    if (game.expansions.any { it.id == k.id && it.isOwned }) k.copy(isOwned = true) else k
                                }
                            }
                        } else enriched?.expansions ?: emptyList()
                    )
                    dao.insertGame(gameToSave.toEntity())
                    count++
                }
            } catch (e: Exception) {
                firebaseManager.logError(e, "Failed to insert game: ${game.title}")
            }
        }
        return count
    }

    override suspend fun fetchBggUserProfile(username: String): String? {
        ensureSession()
        return try {
            val response = api.getUser(username)
            response.avatarLink?.value?.takeIf { it.isNotBlank() && it != "N/A" }
        } catch (e: Exception) {
            firebaseManager.logError(e, "BGG User Fetch Error: $username")
            null
        }
    }

    override suspend fun exportCollectionToJson(collectionId: String): String {
        return withContext(Dispatchers.IO) {
            val entities = dao.getAllGamesSync(collectionId)
            val games = entities.map { it.toDomainModel() }
            val mapper = jacksonObjectMapper()
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(games)
        }
    }

    override suspend fun exportCollectionToCsv(collectionId: String): String {
        return withContext(Dispatchers.IO) {
            val entities = dao.getAllGamesSync(collectionId)
            val games = entities.map { it.toDomainModel() }
            
            val sb = StringBuilder()
            sb.append("ID,Title,Year,MinPlayers,MaxPlayers,Time,Owned,Wishlist,Favorite,Notes,Categories,Website,Thumbnail,Image,Description,Expansions\n")
            
            games.forEach { g ->
                val row = listOf(
                    g.id,
                    escapeCsv(g.title),
                    g.yearPublished ?: "",
                    g.minPlayers ?: "",
                    g.maxPlayers ?: "",
                    g.playingTime ?: "",
                    g.isOwned,
                    g.isWishlisted,
                    g.isFavorite,
                    escapeCsv(g.notes ?: ""),
                    escapeCsv(g.categories.joinToString("|")),
                    g.websiteUrl ?: "",
                    g.thumbnailUrl ?: "",
                    g.imageUrl ?: "",
                    escapeCsv(g.description ?: ""),
                    escapeCsv(mapper.writeValueAsString(g.expansions))
                )
                sb.append(row.joinToString(",")).append("\n")
            }
            sb.toString()
        }
    }

    override suspend fun parseCsv(csv: String): List<Game> {
        return withContext(Dispatchers.IO) {
            val rows = mutableListOf<List<String>>()
            val curRow = mutableListOf<String>()
            val curField = StringBuilder()
            var inQuotes = false
            var i = 0
            
            // Proper CSV parsing according to RFC 4180
            while (i < csv.length) {
                val c = csv[i]
                when {
                    c == '\"' -> {
                        if (inQuotes && i + 1 < csv.length && csv[i + 1] == '\"') {
                            curField.append('\"')
                            i++
                        } else {
                            inQuotes = !inQuotes
                        }
                    }
                    c == ',' && !inQuotes -> {
                        curRow.add(curField.toString())
                        curField.setLength(0)
                    }
                    (c == '\n' || c == '\r') && !inQuotes -> {
                        if (c == '\r' && i + 1 < csv.length && csv[i + 1] == '\n') i++
                        curRow.add(curField.toString())
                        curField.setLength(0)
                        if (curRow.isNotEmpty()) {
                            rows.add(curRow.toList())
                            curRow.clear()
                        }
                    }
                    else -> curField.append(c)
                }
                i++
            }
            // Add last field/row if exists
            if (curField.isNotEmpty() || curRow.isNotEmpty()) {
                curRow.add(curField.toString())
                rows.add(curRow)
            }

            if (rows.size <= 1) return@withContext emptyList()
            
            val header = rows[0]
            val colMap = header.mapIndexed { index, s -> s.trim().lowercase() to index }.toMap()
            
            fun getVal(row: List<String>, key: String): String? {
                val idx = colMap[key.lowercase()] ?: return null
                return row.getOrNull(idx)?.takeIf { it.isNotBlank() }
            }

            val games = mutableListOf<Game>()
            rows.drop(1).forEach { parts ->
                try {
                    val id = getVal(parts, "ID")
                    if (id != null && id.all { it.isDigit() }) {
                        val game = Game(
                            id = id,
                            title = getVal(parts, "Title") ?: "Unknown",
                            yearPublished = getVal(parts, "Year"),
                            minPlayers = getVal(parts, "MinPlayers"),
                            maxPlayers = getVal(parts, "MaxPlayers"),
                            playingTime = getVal(parts, "Time"),
                            isOwned = getVal(parts, "Owned")?.toBoolean() ?: true,
                            isWishlisted = getVal(parts, "Wishlist")?.toBoolean() ?: false,
                            isFavorite = getVal(parts, "Favorite")?.toBoolean() ?: false,
                            notes = getVal(parts, "Notes"),
                            categories = getVal(parts, "Categories")?.split("|")?.filter { it.isNotBlank() } ?: emptyList(),
                            websiteUrl = getVal(parts, "Website"),
                            thumbnailUrl = getVal(parts, "Thumbnail"),
                            imageUrl = getVal(parts, "Image"),
                            description = getVal(parts, "Description"),
                            expansions = try {
                                val expJson = getVal(parts, "Expansions")
                                if (!expJson.isNullOrBlank()) mapper.readValue(expJson) else emptyList()
                            } catch (e: Exception) { emptyList() }
                        )
                        games.add(game)
                    }
                } catch (e: Exception) { }
            }
            mergeExpansionsIntelligently(games)
        }
    }

    override suspend fun importRemoteCollection(url: String, name: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var downloadUrl = transformGoogleDriveUrl(url)
            android.util.Log.d("PLANSZOWSKY_DEBUG", "Starting import from: $downloadUrl")

            var request = Request.Builder().url(downloadUrl).build()
            var response = okHttpClient.newCall(request).execute()
            
            if (!response.isSuccessful) return@withContext Result.failure(Exception("Failed to download: ${response.code}"))
            
            var content = response.body.string()
            
            // Check if we got Google Drive virus scan warning (HTML) instead of CSV
            if (content.contains("google.com/uc") && content.contains("confirm=")) {
                android.util.Log.d("PLANSZOWSKY_DEBUG", "Detected Google Drive large file warning. Retrying with confirm token...")
                val confirmToken = Regex("confirm=([^&\"\\s]+)").find(content)?.groupValues?.get(1)
                if (confirmToken != null) {
                    downloadUrl = if (downloadUrl.contains("confirm=")) {
                        downloadUrl.replace(Regex("confirm=[^&]+"), "confirm=$confirmToken")
                    } else {
                        "$downloadUrl&confirm=$confirmToken"
                    }
                    android.util.Log.d("PLANSZOWSKY_DEBUG", "New download URL: $downloadUrl")
                    request = Request.Builder().url(downloadUrl).build()
                    response = okHttpClient.newCall(request).execute()
                    if (!response.isSuccessful) return@withContext Result.failure(Exception("Failed after confirm: ${response.code}"))
                    content = response.body.string()
                }
            }

            android.util.Log.d("PLANSZOWSKY_DEBUG", "Final content length: ${content.length}")
            if (content.isEmpty()) return@withContext Result.failure(Exception("Empty body from server"))

            val games = parseCsv(content)
            android.util.Log.d("PLANSZOWSKY_DEBUG", "Games parsed: ${games.size}")
            
            val collectionId = java.util.UUID.randomUUID().toString()
            val collection = pl.pointblank.planszowsky.data.local.CollectionEntity(
                id = collectionId,
                name = name,
                sourceUrl = url,
                isReadOnly = true,
                lastUpdated = System.currentTimeMillis()
            )
            
            dao.insertCollection(collection)
            val count = saveImportedGames(games, overwriteExisting = true, collectionId = collectionId)
            Result.success(count)
        } catch (e: Exception) {
            android.util.Log.e("PLANSZOWSKY_DEBUG", "Import Error", e)
            Result.failure(e)
        }
    }

    override suspend fun refreshCollection(collectionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val collection = dao.getCollectionById(collectionId) ?: return@withContext Result.failure(Exception("Not found"))
            val url = collection.sourceUrl ?: return@withContext Result.failure(Exception("No URL"))
            
            val downloadUrl = transformGoogleDriveUrl(url)
            val request = Request.Builder().url(downloadUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext Result.failure(Exception("Failed: ${response.code}"))
            
            val csvContent = response.body.string()
            val games = parseCsv(csvContent)
            
            dao.deleteGamesByCollection(collectionId)
            saveImportedGames(games, overwriteExisting = true, collectionId = collectionId)
            
            dao.insertCollection(collection.copy(lastUpdated = System.currentTimeMillis()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun transformGoogleDriveUrl(url: String): String {
        return if (url.contains("drive.google.com")) {
            val fileId = Regex("/d/([^/]+)").find(url)?.groupValues?.get(1)
            if (fileId != null) {
                // Add confirm=t to bypass some of Google's virus scan warnings for "large" files
                "https://drive.google.com/uc?export=download&id=$fileId&confirm=t"
            } else url
        } else url
    }

    override fun getAllCollections(): Flow<List<pl.pointblank.planszowsky.data.local.CollectionEntity>> = dao.getAllCollections()

    override suspend fun deleteCollection(collectionId: String) {
        if (collectionId == "main") return
        val collection = dao.getCollectionById(collectionId)
        if (collection != null) {
            val gamesInCollection = dao.getAllGamesSync(collectionId)
            gamesInCollection.forEach { 
                pl.pointblank.planszowsky.util.ImageManager.deleteImage(it.localImageUri)
            }
            dao.deleteGamesByCollection(collectionId)
            dao.deleteCollection(collection)
        }
    }

    override suspend fun renameCollection(collectionId: String, newName: String) {
        dao.updateCollectionName(collectionId, newName)
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val cur = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    cur.append('\"')
                    i++
                } else inQuotes = !inQuotes
            } else if (c == ',' && !inQuotes) {
                result.add(cur.toString())
                cur.setLength(0)
            } else cur.append(c)
            i++
        }
        result.add(cur.toString())
        return result
    }

    private fun escapeCsv(value: String): String {
        if (!value.contains(",") && !value.contains("\"") && !value.contains("\n")) return value
        return "\"" + value.replace("\"", "\"\"") + "\""
    }
}
