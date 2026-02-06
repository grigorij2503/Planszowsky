package com.planszowsky.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planszowsky.android.domain.model.AppTheme
import com.planszowsky.android.domain.model.Game
import com.planszowsky.android.domain.repository.GameRepository
import com.planszowsky.android.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val repository: GameRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = userPreferencesRepository.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.MODERN)

    val wishlistedGames: StateFlow<List<Game>> = repository.getWishlistedGames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
