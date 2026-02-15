package pl.pointblank.planszowsky.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.model.CollectionViewMode
import pl.pointblank.planszowsky.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val COLLECTION_VIEW_MODE = stringPreferencesKey("collection_view_mode")
        val AI_USAGE_COUNT = intPreferencesKey("ai_usage_count")
        val LAST_AI_USAGE_TIMESTAMP = longPreferencesKey("last_ai_usage_timestamp")
        val APP_LOCALE = stringPreferencesKey("app_locale")
        val BGG_AVATAR_URL = stringPreferencesKey("bgg_avatar_url")
        val BGG_USERNAME = stringPreferencesKey("bgg_username")
    }

    override val appTheme: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.APP_THEME] ?: AppTheme.MODERN.name
            try {
                AppTheme.valueOf(themeName)
            } catch (_: IllegalArgumentException) {
                AppTheme.MODERN
            }
        }

    override val collectionViewMode: Flow<CollectionViewMode> = context.dataStore.data
        .map { preferences ->
            val modeName = preferences[PreferencesKeys.COLLECTION_VIEW_MODE] ?: CollectionViewMode.GRID.name
            try {
                CollectionViewMode.valueOf(modeName)
            } catch (_: IllegalArgumentException) {
                CollectionViewMode.GRID
            }
        }

    override val aiUsageCount: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.AI_USAGE_COUNT] ?: 0 }

    override val lastAiUsageTimestamp: Flow<Long> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.LAST_AI_USAGE_TIMESTAMP] ?: 0L }

    override val appLocale: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.APP_LOCALE] ?: "system" }

    override val bggAvatarUrl: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.BGG_AVATAR_URL] }

    override val bggUsername: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.BGG_USERNAME] }

    override suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = theme.name
        }
    }

    override suspend fun setCollectionViewMode(mode: CollectionViewMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLLECTION_VIEW_MODE] = mode.name
        }
    }

    override suspend fun setAppLocale(locale: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LOCALE] = locale
        }
    }

    override suspend fun setBggAvatarUrl(url: String?) {
        context.dataStore.edit { preferences ->
            if (url != null) {
                preferences[PreferencesKeys.BGG_AVATAR_URL] = url
            } else {
                preferences.remove(PreferencesKeys.BGG_AVATAR_URL)
            }
        }
    }

    override suspend fun setBggUsername(username: String?) {
        context.dataStore.edit { preferences ->
            if (username != null) {
                preferences[PreferencesKeys.BGG_USERNAME] = username
            } else {
                preferences.remove(PreferencesKeys.BGG_USERNAME)
            }
        }
    }

    override suspend fun incrementAiUsage(resetIfNewDay: Boolean) {
        context.dataStore.edit { preferences ->
            val currentCount = preferences[PreferencesKeys.AI_USAGE_COUNT] ?: 0
            val lastTimestamp = preferences[PreferencesKeys.LAST_AI_USAGE_TIMESTAMP] ?: 0L
            val now = System.currentTimeMillis()

            if (resetIfNewDay && isNewDay(lastTimestamp, now)) {
                preferences[PreferencesKeys.AI_USAGE_COUNT] = 1
            } else {
                preferences[PreferencesKeys.AI_USAGE_COUNT] = currentCount + 1
            }
            preferences[PreferencesKeys.LAST_AI_USAGE_TIMESTAMP] = now
        }
    }

    override suspend fun resetAiUsageIfNewDay() {
        context.dataStore.edit { preferences ->
            val lastTimestamp = preferences[PreferencesKeys.LAST_AI_USAGE_TIMESTAMP] ?: 0L
            val now = System.currentTimeMillis()

            if (isNewDay(lastTimestamp, now)) {
                preferences[PreferencesKeys.AI_USAGE_COUNT] = 0
                // We don't update timestamp here to avoid blocking next usage if reset fails or app closes
            }
        }
    }

    private fun isNewDay(lastTimestamp: Long, currentTimestamp: Long): Boolean {
        if (lastTimestamp == 0L) return true
        val lastCalendar = Calendar.getInstance().apply { timeInMillis = lastTimestamp }
        val currentCalendar = Calendar.getInstance().apply { timeInMillis = currentTimestamp }
        
        return lastCalendar.get(Calendar.DAY_OF_YEAR) != currentCalendar.get(Calendar.DAY_OF_YEAR) ||
                lastCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR)
    }
}
