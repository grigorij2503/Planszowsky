package pl.pointblank.planszowsky.domain.repository

import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.model.CollectionViewMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val appTheme: Flow<AppTheme>
    val collectionViewMode: Flow<CollectionViewMode>
    val aiUsageCount: Flow<Int>
    val lastAiUsageTimestamp: Flow<Long>

    suspend fun setAppTheme(theme: AppTheme)
    suspend fun setCollectionViewMode(mode: CollectionViewMode)
    suspend fun incrementAiUsage(resetIfNewDay: Boolean)
}
