package com.planszowsky.android.domain.repository

import com.planszowsky.android.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val appTheme: Flow<AppTheme>
    suspend fun setAppTheme(theme: AppTheme)
}
