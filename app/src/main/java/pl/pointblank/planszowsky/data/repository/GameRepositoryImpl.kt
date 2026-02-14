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

class GameRepositoryImpl @Inject constructor(
    private val dao: GameDao,
    private val api: BggApi,
    private val okHttpClient: OkHttpClient,
    private val firebaseManager: FirebaseManager
) : GameRepository {

    override fun getCollectionStats(): Flow<CollectionStats> {
        return combine(
            dao.getOwnedCount(),
            dao.getWishlistCount(),
            dao.getFavoriteCount(),
            dao.getLentCount(),
            dao.getAllOwnedCategories()
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

    override fun getSavedGames(): Flow<List<Game>> {
        return dao.getAllGames().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getWishlistedGames(): Flow<List<Game>> {
        return dao.getWishlistedGames().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getGame(id: String): Game? {
        return dao.getGameById(id)?.toDomainModel()
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


    override suspend fun updateNotes(gameId: String, notes: String) {
        val game = getGame(gameId)
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
                            ownerId = username,
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
                            ownerId = username
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

    override suspend fun saveImportedGames(games: List<Game>, overwriteExisting: Boolean): Int {
        var count = 0
        games.forEach { game ->
            val existing = dao.getGameById(game.id)
            if (existing == null || overwriteExisting) {
                dao.insertGame(game.toEntity())
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
}