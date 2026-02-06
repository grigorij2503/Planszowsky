package pl.pointblank.planszowsky.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.model.Game
import pl.pointblank.planszowsky.domain.repository.GameRepository
import pl.pointblank.planszowsky.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: GameRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = userPreferencesRepository.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.MODERN)

    private val gameId: String = checkNotNull(savedStateHandle["gameId"])

    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game.asStateFlow()

    init {
        viewModelScope.launch {
            _game.value = repository.getGame(gameId)
        }
    }
    
    fun deleteGame() {
        viewModelScope.launch {
            _game.value?.let {
                repository.deleteGame(it)
            }
        }
    }

    fun toggleWishlist() {
        viewModelScope.launch {
            _game.value?.let { currentNewGame ->
                repository.toggleWishlist(currentNewGame)
                _game.value = repository.getGame(gameId)
            }
        }
    }

    fun updateBorrowedStatus(isBorrowed: Boolean, borrowedTo: String?) {
        viewModelScope.launch {
            _game.value?.let { current ->
                val updated = current.copy(isBorrowed = isBorrowed, borrowedTo = borrowedTo)
                repository.updateGame(updated)
                _game.value = repository.getGame(gameId)
            }
        }
    }
}
