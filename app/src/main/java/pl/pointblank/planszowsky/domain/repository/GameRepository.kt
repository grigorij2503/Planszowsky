package pl.pointblank.planszowsky.domain.repository

import pl.pointblank.planszowsky.domain.model.CollectionStats
import pl.pointblank.planszowsky.domain.model.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getSavedGames(collectionId: String = "main"): Flow<List<Game>>
    fun getWishlistedGames(collectionId: String = "main"): Flow<List<Game>>
    fun getCollectionStats(collectionId: String = "main"): Flow<CollectionStats>
    suspend fun getGame(id: String, collectionId: String = "main"): Game?
    suspend fun saveGame(game: Game)
    suspend fun updateGame(game: Game)
    suspend fun toggleWishlist(game: Game)
    suspend fun toggleFavorite(game: Game)
    suspend fun deleteGame(game: Game)
    suspend fun searchRemoteGames(query: String): List<Game>
    suspend fun searchByBarcode(barcode: String): List<Game>
    suspend fun getRemoteGameDetails(id: String, preferredTitle: String? = null): Game?
    suspend fun updateNotes(gameId: String, collectionId: String = "main", notes: String)
    suspend fun fetchCollection(username: String): List<Game>
    suspend fun saveImportedGames(games: List<Game>, overwriteExisting: Boolean, collectionId: String = "main"): Int
    suspend fun fetchBggUserProfile(username: String): String?
    suspend fun exportCollectionToJson(collectionId: String = "main"): String
    suspend fun exportCollectionToCsv(collectionId: String = "main"): String
    suspend fun parseCsv(csv: String): List<Game>

    // Collections
    suspend fun importRemoteCollection(url: String, name: String): Result<Int>
    suspend fun refreshCollection(collectionId: String): Result<Unit>
    fun getAllCollections(): Flow<List<pl.pointblank.planszowsky.data.local.CollectionEntity>>
    suspend fun deleteCollection(collectionId: String)
}
