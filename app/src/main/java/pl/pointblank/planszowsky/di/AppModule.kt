package pl.pointblank.planszowsky.di

import android.content.Context
import androidx.room.Room
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import pl.pointblank.planszowsky.data.local.AppDatabase
import pl.pointblank.planszowsky.data.local.GameDao
import pl.pointblank.planszowsky.data.remote.BggApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
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
        )
        .addMigrations(
            AppDatabase.MIGRATION_5_6, 
            AppDatabase.MIGRATION_6_7, 
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9
        )
        .build()
    }

    @Provides
    fun provideGameDao(database: AppDatabase): GameDao {
        return database.gameDao()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("User-Agent", "Planszowsky/0.1 (Android)")
                
                if (pl.pointblank.planszowsky.BuildConfig.BGG_API_KEY.isNotBlank()) {
                    requestBuilder.header("Authorization", "Bearer ${pl.pointblank.planszowsky.BuildConfig.BGG_API_KEY}")
                }
                
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideBggApi(okHttpClient: OkHttpClient): BggApi {
        val xmlMapper = XmlMapper()
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        return Retrofit.Builder()
            .baseUrl("https://boardgamegeek.com/xmlapi2/")
            .client(okHttpClient)
            .addConverterFactory(JacksonConverterFactory.create(xmlMapper))
            .build()
            .create(BggApi::class.java)
    }
}
