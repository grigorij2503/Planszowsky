package pl.pointblank.planszowsky.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.model.CollectionViewMode
import pl.pointblank.planszowsky.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
}
