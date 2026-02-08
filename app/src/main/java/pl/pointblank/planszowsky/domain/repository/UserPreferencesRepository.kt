package pl.pointblank.planszowsky.domain.repository

import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.model.CollectionViewMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val appTheme: Flow<AppTheme>
    val collectionViewMode: Flow<CollectionViewMode>
    suspend fun setAppTheme(theme: AppTheme)
    suspend fun setCollectionViewMode(mode: CollectionViewMode)
}
