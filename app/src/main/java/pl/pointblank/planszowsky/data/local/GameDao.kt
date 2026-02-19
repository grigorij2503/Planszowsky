package pl.pointblank.planszowsky.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE collectionId = :collectionId AND isWishlisted = 0 ORDER BY isFavorite DESC, title ASC")
    fun getAllGames(collectionId: String = "main"): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE collectionId = :collectionId AND isWishlisted = 1 ORDER BY title ASC")
    fun getWishlistedGames(collectionId: String = "main"): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id AND collectionId = :collectionId")
    suspend fun getGameById(id: String, collectionId: String = "main"): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("SELECT COUNT(*) FROM games WHERE collectionId = :collectionId AND isWishlisted = 0")
    fun getOwnedCount(collectionId: String = "main"): Flow<Int>

    @Query("SELECT COUNT(*) FROM games WHERE collectionId = :collectionId AND isWishlisted = 1")
    fun getWishlistCount(collectionId: String = "main"): Flow<Int>

    @Query("SELECT COUNT(*) FROM games WHERE collectionId = :collectionId AND isFavorite = 1")
    fun getFavoriteCount(collectionId: String = "main"): Flow<Int>

    @Query("SELECT COUNT(*) FROM games WHERE collectionId = :collectionId AND isBorrowed = 1")
    fun getLentCount(collectionId: String = "main"): Flow<Int>

    @Query("SELECT categories FROM games WHERE collectionId = :collectionId AND isWishlisted = 0")
    fun getAllOwnedCategories(collectionId: String = "main"): Flow<List<String>>

    @Query("SELECT * FROM games WHERE collectionId = :collectionId")
    suspend fun getAllGamesSync(collectionId: String = "main"): List<GameEntity>

    @Query("DELETE FROM games WHERE collectionId = :collectionId")
    suspend fun deleteGamesByCollection(collectionId: String)

    // --- Collections ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity)

    @Query("SELECT * FROM collections")
    fun getAllCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getCollectionById(id: String): CollectionEntity?

    @Query("UPDATE collections SET name = :newName WHERE id = :id")
    suspend fun updateCollectionName(id: String, newName: String)

    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)
}
