package com.planszowsky.android.domain.repository

import com.planszowsky.android.domain.model.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getSavedGames(): Flow<List<Game>>
    fun getWishlistedGames(): Flow<List<Game>>
    suspend fun getGame(id: String): Game?
    suspend fun saveGame(game: Game)
    suspend fun toggleWishlist(game: Game)
    suspend fun deleteGame(game: Game)
    suspend fun searchRemoteGames(query: String): List<Game>
    suspend fun getRemoteGameDetails(id: String): Game?
}
