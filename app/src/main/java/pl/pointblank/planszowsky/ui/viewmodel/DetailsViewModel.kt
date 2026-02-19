package pl.pointblank.planszowsky.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.model.Game
import pl.pointblank.planszowsky.domain.repository.GameRepository
import pl.pointblank.planszowsky.domain.repository.UserPreferencesRepository
import pl.pointblank.planszowsky.util.FirebaseManager
import pl.pointblank.planszowsky.util.TranslationManager
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
    private val translationManager: TranslationManager,
    userPreferencesRepository: UserPreferencesRepository,
    firebaseManager: FirebaseManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = userPreferencesRepository.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.MODERN)

    val isExpertChatEnabled: StateFlow<Boolean> = firebaseManager.isExpertChatEnabled

    private val gameId: String = checkNotNull(savedStateHandle["gameId"])
    private val collectionId: String = savedStateHandle["collectionId"] ?: "main"

    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val _translatedDescription = MutableStateFlow<String?>(null)
    val translatedDescription: StateFlow<String?> = _translatedDescription.asStateFlow()

    init {
        viewModelScope.launch {
            _game.value = repository.getGame(gameId, collectionId)
        }
    }

    fun translateDescription() {
        val currentDescription = _game.value?.description ?: return
        if (_translatedDescription.value != null) {
            _translatedDescription.value = null // Toggle off
            return
        }

        viewModelScope.launch {
            _isTranslating.value = true
            val result = translationManager.translate(currentDescription)
            result.onSuccess {
                _translatedDescription.value = it
            }.onFailure {
                // For now, just stop loading. In a real app, show error.
            }
            _isTranslating.value = false
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
                _game.value = repository.getGame(gameId, collectionId)
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            _game.value?.let { current ->
                repository.toggleFavorite(current)
                _game.value = repository.getGame(gameId, collectionId)
            }
        }
    }

    fun updateBorrowingInfo(
        isBorrowed: Boolean, 
        borrowedTo: String?, 
        isBorrowedFrom: Boolean, 
        borrowedFrom: String?
    ) {
        viewModelScope.launch {
            _game.value?.let { current ->
                val updated = current.copy(
                    isBorrowed = isBorrowed,
                    borrowedTo = borrowedTo,
                    isBorrowedFrom = isBorrowedFrom,
                    borrowedFrom = borrowedFrom
                )
                repository.updateGame(updated)
                _game.value = repository.getGame(gameId, collectionId)
            }
        }
    }

    fun updateNotes(notes: String) {
        viewModelScope.launch {
            repository.updateNotes(gameId, collectionId, notes)
            _game.value = repository.getGame(gameId, collectionId)
        }
    }

    fun toggleExpansion(expansionId: String) {
        viewModelScope.launch {
            _game.value?.let { current ->
                val updatedExpansions = current.expansions.map {
                    if (it.id == expansionId) it.copy(isOwned = !it.isOwned) else it
                }
                val updatedGame = current.copy(expansions = updatedExpansions)
                repository.updateGame(updatedGame)
                _game.value = repository.getGame(gameId, collectionId)
            }
        }
    }
}
