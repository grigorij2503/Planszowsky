package pl.pointblank.planszowsky.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface BggApi {
    @GET("search")
    suspend fun searchGames(
        @Query("query") query: String,
        @Query("type") type: String = "boardgame"
    ): BggSearchResponse

    @GET("thing")
    suspend fun getGameDetails(
        @Query("id") id: String
    ): BggThingResponse
}
