package com.planszowsky.android.data.repository

import com.planszowsky.android.data.local.GameDao
import com.planszowsky.android.data.local.toDomainModel
import com.planszowsky.android.data.local.toEntity
import com.planszowsky.android.data.remote.BggApi
import com.planszowsky.android.domain.model.Game
import com.planszowsky.android.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor(
    private val dao: GameDao,
    private val api: BggApi,
    private val okHttpClient: OkHttpClient
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
        dao.insertGame(game.copy(isWishlisted = !game.isWishlisted).toEntity())
    }

    override suspend fun deleteGame(game: Game) {
        dao.deleteGame(game.toEntity())
    }

    override suspend fun searchRemoteGames(query: String): List<Game> {
        ensureSession()
        return try {
            val response = api.searchGames(query)
            response.items?.map { item ->
                Game(
                    id = item.id,
                    title = item.name?.value ?: "Unknown",
                    yearPublished = item.yearPublished?.value
                )
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
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
                isOwned = false 
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
