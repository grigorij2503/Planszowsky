package pl.pointblank.planszowsky.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE isWishlisted = 0 ORDER BY isFavorite DESC, title ASC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isWishlisted = 1 ORDER BY title ASC")
    fun getWishlistedGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameById(id: String): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("SELECT COUNT(*) FROM games WHERE isWishlisted = 0")
    fun getOwnedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM games WHERE isWishlisted = 1")
    fun getWishlistCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM games WHERE isFavorite = 1")
    fun getFavoriteCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM games WHERE isBorrowed = 1")
    fun getLentCount(): Flow<Int>

    @Query("SELECT categories FROM games WHERE isWishlisted = 0")
    fun getAllOwnedCategories(): Flow<List<String>>

    @Query("SELECT * FROM games")
    suspend fun getAllGamesSync(): List<GameEntity>
}
