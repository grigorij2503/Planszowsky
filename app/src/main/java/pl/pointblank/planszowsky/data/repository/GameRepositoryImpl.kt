package pl.pointblank.planszowsky.data.repository

import pl.pointblank.planszowsky.data.local.GameDao
import pl.pointblank.planszowsky.data.local.toDomainModel
import pl.pointblank.planszowsky.data.local.toEntity
import pl.pointblank.planszowsky.data.remote.BggApi
import pl.pointblank.planszowsky.domain.model.Game
import pl.pointblank.planszowsky.domain.repository.GameRepository
import pl.pointblank.planszowsky.util.FirebaseManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.HttpException
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor(
    private val dao: GameDao,
    private val api: BggApi,
    private val okHttpClient: OkHttpClient,
    private val firebaseManager: FirebaseManager
) : GameRepository {

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
                val primaryName = item.names?.find { it.type == "primary" }?.value 
                    ?: item.names?.firstOrNull()?.value 
                    ?: "Unknown"
                
                Game(
                    id = item.id,
                    title = primaryName,
                    thumbnailUrl = item.thumbnail,
                    imageUrl = item.image,
                    yearPublished = item.yearPublished?.value
                )
            } ?: emptyList()
        } catch (e: HttpException) {
            firebaseManager.logError(e, "BGG Search Error (${e.code()}): $query")
            emptyList()
        } catch (e: Exception) {
            firebaseManager.logError(e, "BGG Search Exception: $query")
            emptyList()
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
                    title = primaryName,
                    thumbnailUrl = item.thumbnail,
                    imageUrl = item.image,
                    yearPublished = item.yearPublished?.value
                )
            } ?: emptyList()
        } catch (e: HttpException) {
            firebaseManager.logError(e, "BGG Barcode Error (${e.code()}): $barcode")
            emptyList()
        } catch (e: Exception) {
            firebaseManager.logError(e, "BGG Barcode Exception: $barcode")
            emptyList()
        }
    }

    override suspend fun getRemoteGameDetails(id: String): Game? {
        ensureSession()
        return try {
            val response = api.getGameDetails(id)
            val item = response.items?.firstOrNull() ?: return null
            
            val primaryName = item.names?.find { it.type == "primary" }?.value 
                ?: item.names?.firstOrNull()?.value 
                ?: "Unknown"

            val categories = item.links?.filter { it.type == "boardgamecategory" }?.map { it.value } ?: emptyList()

            Game(
                id = item.id,
                title = primaryName,
                thumbnailUrl = item.thumbnail,
                imageUrl = item.image,
                description = item.description,
                yearPublished = item.yearPublished?.value,
                minPlayers = item.minPlayers?.value,
                maxPlayers = item.maxPlayers?.value,
                playingTime = item.playingTime?.value,
                isOwned = false,
                categories = categories
            )
        } catch (e: HttpException) {
            firebaseManager.logError(e, "BGG Details Error (${e.code()}): $id")
            null
        } catch (e: Exception) {
            firebaseManager.logError(e, "BGG Details Exception: $id")
            null
        }
    }

    override suspend fun importCollection(username: String): Int {
        ensureSession()
        var importedCount = 0
        try {
            val response = api.getCollection(username)
            response.items?.forEach { item ->
                val game = Game(
                    id = item.id,
                    title = item.name ?: "Unknown",
                    thumbnailUrl = item.thumbnail,
                    imageUrl = item.image,
                    yearPublished = item.yearPublished,
                    isOwned = item.status?.own == "1",
                    isWishlisted = item.status?.wishlist == "1"
                )
                dao.insertGame(game.toEntity())
                importedCount++
            }
        } catch (e: HttpException) {
            firebaseManager.logError(e, "BGG Import Error (${e.code()}): $username")
            throw e
        } catch (e: Exception) {
            firebaseManager.logError(e, "BGG Import Exception: $username")
            throw e
        }
        return importedCount
    }
}
