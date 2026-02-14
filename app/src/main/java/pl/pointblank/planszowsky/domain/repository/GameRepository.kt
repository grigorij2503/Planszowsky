package pl.pointblank.planszowsky.domain.repository

import pl.pointblank.planszowsky.domain.model.CollectionStats
import pl.pointblank.planszowsky.domain.model.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getSavedGames(): Flow<List<Game>>
    fun getWishlistedGames(): Flow<List<Game>>
    fun getCollectionStats(): Flow<CollectionStats>
    suspend fun getGame(id: String): Game?
    suspend fun saveGame(game: Game)
    suspend fun updateGame(game: Game)
    suspend fun toggleWishlist(game: Game)
    suspend fun toggleFavorite(game: Game)
    suspend fun deleteGame(game: Game)
    suspend fun searchRemoteGames(query: String): List<Game>
    suspend fun searchByBarcode(barcode: String): List<Game>
    suspend fun getRemoteGameDetails(id: String, preferredTitle: String? = null): Game?
    suspend fun updateNotes(gameId: String, notes: String)
    suspend fun importCollection(username: String): Int
    suspend fun fetchBggUserProfile(username: String): String?
}
