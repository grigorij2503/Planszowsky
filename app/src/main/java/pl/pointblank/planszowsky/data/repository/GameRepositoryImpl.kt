package pl.pointblank.planszowsky.data.repository

import pl.pointblank.planszowsky.data.local.GameDao
import pl.pointblank.planszowsky.data.local.toDomainModel
import pl.pointblank.planszowsky.data.local.toEntity
import pl.pointblank.planszowsky.data.remote.BggApi
import pl.pointblank.planszowsky.domain.model.Game
import pl.pointblank.planszowsky.domain.repository.GameRepository
import pl.pointblank.planszowsky.util.FirebaseManager
import pl.pointblank.planszowsky.util.decodeHtml
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

class GameRepositoryImpl @Inject constructor(
    private val dao: GameDao,
    private val api: BggApi,
    private val okHttpClient: OkHttpClient,
    private val firebaseManager: FirebaseManager
) : GameRepository {

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
        // If moving TO wishlist, it's NOT in collection (isOwned = false)
        // If moving FROM wishlist, it's back in collection (isOwned = true)
        dao.insertGame(game.copy(
            isWishlisted = nextWishlistState,
            isOwned = !nextWishlistState
        ).toEntity())
    }

    override suspend fun toggleFavorite(game: Game) {
        dao.insertGame(game.copy(isFavorite = !game.isFavorite).toEntity())
    }

    override suspend fun deleteGame(game: Game) {
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

    override suspend fun fetchCollection(username: String): List<Game> {
        ensureSession()
        val importedGames = mutableListOf<Game>()
        try {
            val response = api.getCollection(username)
            val items = response.items ?: emptyList()
            if (items.isEmpty()) return emptyList()

            val chunkedItems = items.chunked(20)

            chunkedItems.forEach { batch ->
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
                        importedGames.add(game)
                    }
                } catch (e: Exception) {
                    batch.forEach { item ->
                        val game = Game(
                            id = item.id,
                            title = item.name?.decodeHtml() ?: "Unknown",
                            thumbnailUrl = item.thumbnail,
                            imageUrl = item.image,
                            yearPublished = item.yearPublished,
                            minPlayers = item.stats?.minplayers,
                            maxPlayers = item.stats?.maxplayers,
                            playingTime = item.stats?.playingtime,
                            notes = item.comment,
                            isOwned = item.status?.own == "1",
                            isWishlisted = item.status?.wishlist == "1",
                            collectionId = "main"
                        )
                        importedGames.add(game)
                    }
                }
            }
        } catch (e: HttpException) {
            firebaseManager.logError(e, "BGG Fetch Error (${e.code()}): $username")
            throw e
        } catch (e: Exception) {
            firebaseManager.logError(e, "BGG Fetch Exception: $username")
            throw e
        }
        return importedGames
    }

    override suspend fun saveImportedGames(games: List<Game>, overwriteExisting: Boolean, collectionId: String): Int {
        var count = 0
        
        // Find games that are missing crucial metadata
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
                            categories = item.links?.filter { it.type == "boardgamecategory" }?.map { it.value } ?: emptyList()
                        )
                    }
                } catch (e: Exception) {
                    firebaseManager.logError(e, "Enrichment Error during import")
                }
            }
        }

        games.forEach { game ->
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
                    categories = game.categories.takeIf { it.isNotEmpty() } ?: enriched?.categories ?: emptyList()
                )
                dao.insertGame(gameToSave.toEntity())
                count++
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

    override suspend fun exportCollectionToJson(): String {
        return withContext(Dispatchers.IO) {
            val entities = dao.getAllGamesSync()
            val games = entities.map { it.toDomainModel() }
            val mapper = jacksonObjectMapper()
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(games)
        }
    }

    override suspend fun exportCollectionToCsv(): String {
        return withContext(Dispatchers.IO) {
            val entities = dao.getAllGamesSync()
            val games = entities.map { it.toDomainModel() }
            
            val sb = StringBuilder()
            // Header
            sb.append("ID,Title,Year,MinPlayers,MaxPlayers,Time,Owned,Wishlist,Favorite,Notes,Categories,Website,Thumbnail,Image,Description\n")
            
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
                    escapeCsv(g.description ?: "")
                )
                sb.append(row.joinToString(",")).append("\n")
            }
            sb.toString()
        }
    }

    override suspend fun parseCsv(csv: String): List<Game> {
        return withContext(Dispatchers.IO) {
            val lines = csv.lines()
            if (lines.size <= 1) return@withContext emptyList()
            
            val games = mutableListOf<Game>()
            // Skip header
            lines.drop(1).filter { it.isNotBlank() }.forEach { line ->
                try {
                    val parts = parseCsvLine(line)
                    if (parts.size >= 9) {
                        val game = Game(
                            id = parts[0],
                            title = parts[1],
                            yearPublished = parts[2].takeIf { it.isNotBlank() },
                            minPlayers = parts[3].takeIf { it.isNotBlank() },
                            maxPlayers = parts[4].takeIf { it.isNotBlank() },
                            playingTime = parts[5].takeIf { it.isNotBlank() },
                            isOwned = parts[6].toBoolean(),
                            isWishlisted = parts[7].toBoolean(),
                            isFavorite = parts[8].toBoolean(),
                            notes = parts.getOrNull(9)?.takeIf { it.isNotBlank() },
                            categories = parts.getOrNull(10)?.split("|")?.filter { it.isNotBlank() } ?: emptyList(),
                            websiteUrl = parts.getOrNull(11)?.takeIf { it.isNotBlank() },
                            thumbnailUrl = parts.getOrNull(12)?.takeIf { it.isNotBlank() },
                            imageUrl = parts.getOrNull(13)?.takeIf { it.isNotBlank() },
                            description = parts.getOrNull(14)?.takeIf { it.isNotBlank() }
                        )
                        games.add(game)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            games
        }
    }

    override suspend fun importRemoteCollection(url: String, name: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val downloadUrl = transformGoogleDriveUrl(url)
            val request = Request.Builder().url(downloadUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext Result.failure(Exception("Failed to download collection: ${response.code}"))
            
            val csvContent = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))
            val games = parseCsv(csvContent)
            
            val collectionId = java.util.UUID.randomUUID().toString()
            val collection = pl.pointblank.planszowsky.data.local.CollectionEntity(
                id = collectionId,
                name = name,
                sourceUrl = url, // Keep original URL for reference
                isReadOnly = true,
                lastUpdated = System.currentTimeMillis()
            )
            
            dao.insertCollection(collection)
            val count = saveImportedGames(games, overwriteExisting = true, collectionId = collectionId)
            
            Result.success(count)
        } catch (e: Exception) {
            firebaseManager.logError(e, "Remote Import Error: $url")
            Result.failure(e)
        }
    }

    override suspend fun refreshCollection(collectionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val collection = dao.getCollectionById(collectionId) ?: return@withContext Result.failure(Exception("Collection not found"))
            val url = collection.sourceUrl ?: return@withContext Result.failure(Exception("Collection has no source URL"))
            
            val downloadUrl = transformGoogleDriveUrl(url)
            val request = Request.Builder().url(downloadUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext Result.failure(Exception("Failed to refresh: ${response.code}"))
            
            val csvContent = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
            val games = parseCsv(csvContent)
            
            dao.deleteGamesByCollection(collectionId)
            saveImportedGames(games, overwriteExisting = true, collectionId = collectionId)
            
            dao.insertCollection(collection.copy(lastUpdated = System.currentTimeMillis()))
            
            Result.success(Unit)
        } catch (e: Exception) {
            firebaseManager.logError(e, "Refresh Error: $collectionId")
            Result.failure(e)
        }
    }

    private fun transformGoogleDriveUrl(url: String): String {
        return if (url.contains("drive.google.com")) {
            val fileId = Regex("/d/([^/]+)").find(url)?.groupValues?.get(1)
            if (fileId != null) {
                "https://drive.google.com/uc?export=download&id=$fileId"
            } else url
        } else url
    }

    override fun getAllCollections(): Flow<List<pl.pointblank.planszowsky.data.local.CollectionEntity>> {
        return dao.getAllCollections()
    }

    override suspend fun deleteCollection(collectionId: String) {
        if (collectionId == "main") return // Cannot delete main
        val collection = dao.getCollectionById(collectionId)
        if (collection != null) {
            dao.deleteGamesByCollection(collectionId)
            dao.deleteCollection(collection)
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var cur = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    cur.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(cur.toString())
                cur = StringBuilder()
            } else {
                cur.append(c)
            }
            i++
        }
        result.add(cur.toString())
        return result
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }
}