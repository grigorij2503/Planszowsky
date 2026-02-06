package pl.pointblank.planszowsky.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.model.Game
import pl.pointblank.planszowsky.domain.repository.GameRepository
import pl.pointblank.planszowsky.domain.repository.UserPreferencesRepository
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
