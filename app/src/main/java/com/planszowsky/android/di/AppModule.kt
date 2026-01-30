package com.planszowsky.android.di

import android.content.Context
import androidx.room.Room
import com.planszowsky.android.data.local.AppDatabase
import com.planszowsky.android.data.local.GameDao
import com.planszowsky.android.data.remote.BggApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.planszowsky.android.data.remote.MockBggInterceptor
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "planszowsky_db"
        ).build()
    }

    @Provides
    fun provideGameDao(database: AppDatabase): GameDao {
        return database.gameDao()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            // .cookieJar(...) // CookieJar not needed for mock
            .addInterceptor(MockBggInterceptor()) // <--- MOCK ACTIVE
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideBggApi(okHttpClient: OkHttpClient): BggApi {
        return Retrofit.Builder()
            .baseUrl("https://boardgamegeek.com/xmlapi2/")
            .client(okHttpClient)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
            .create(BggApi::class.java)
    }
}
