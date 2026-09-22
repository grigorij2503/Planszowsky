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
import javax.inject.Qualifier
import pl.pointblank.planszowsky.BuildConfig

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BggHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteImportHttpClient

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
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6, 
            AppDatabase.MIGRATION_6_7, 
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9,
            AppDatabase.MIGRATION_9_10,
            AppDatabase.MIGRATION_10_11,
            AppDatabase.MIGRATION_11_12
        )
        .build()
    }

    @Provides
    fun provideGameDao(database: AppDatabase): GameDao {
        return database.gameDao()
    }

    @Provides
    @Singleton
    @BggHttpClient
    fun provideBggHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36")

                val host = original.url.host.lowercase()
                val isBggHost = host == "boardgamegeek.com" || host.endsWith(".boardgamegeek.com")
                if (isBggHost && BuildConfig.BGG_API_KEY.isNotBlank()) {
                    requestBuilder.header("Authorization", "Bearer ${BuildConfig.BGG_API_KEY}")
                }

                chain.proceed(requestBuilder.build())
            }

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                redactHeader("Authorization")
                level = HttpLoggingInterceptor.Level.HEADERS
            }
            builder.addInterceptor(logging)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    @RemoteImportHttpClient
    fun provideRemoteImportHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    @Provides
    @Singleton
    fun provideBggApi(@BggHttpClient okHttpClient: OkHttpClient): BggApi {
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
