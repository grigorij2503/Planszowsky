package pl.pointblank.planszowsky.domain.repository

import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.model.CollectionViewMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val appTheme: Flow<AppTheme>
    val collectionViewMode: Flow<CollectionViewMode>
    val aiUsageCount: Flow<Int>
    val lastAiUsageTimestamp: Flow<Long>
    val appLocale: Flow<String>
    val bggAvatarUrl: Flow<String?>
    val bggUsername: Flow<String?>

    suspend fun setAppTheme(theme: AppTheme)
    suspend fun setCollectionViewMode(mode: CollectionViewMode)
    suspend fun setAppLocale(locale: String)
    suspend fun setBggAvatarUrl(url: String?)
    suspend fun setBggUsername(username: String?)
    suspend fun incrementAiUsage(resetIfNewDay: Boolean)
    suspend fun resetAiUsageIfNewDay()
}
