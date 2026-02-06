package pl.pointblank.planszowsky.domain.repository

import pl.pointblank.planszowsky.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val appTheme: Flow<AppTheme>
    suspend fun setAppTheme(theme: AppTheme)
}
