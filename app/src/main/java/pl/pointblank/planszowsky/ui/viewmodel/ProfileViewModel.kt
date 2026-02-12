package pl.pointblank.planszowsky.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.model.CollectionStats
import pl.pointblank.planszowsky.domain.repository.GameRepository
import pl.pointblank.planszowsky.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: GameRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = userPreferencesRepository.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.MODERN)

    val stats: StateFlow<CollectionStats> = repository.getCollectionStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CollectionStats())

    private val _bggUsername = MutableStateFlow("")
    val bggUsername: StateFlow<String> = _bggUsername.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importResult = MutableSharedFlow<ImportResult>()
    val importResult: SharedFlow<ImportResult> = _importResult.asSharedFlow()

    sealed class ImportResult {
        data class Success(val count: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }

    fun onUsernameChange(username: String) {
        _bggUsername.value = username
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesRepository.setAppTheme(theme)
        }
    }

    fun importFromBgg() {
        val username = _bggUsername.value
        if (username.isBlank()) return

        viewModelScope.launch {
            _isImporting.value = true
            try {
                val count = repository.importCollection(username)
                _importResult.emit(ImportResult.Success(count))
            } catch (e: Exception) {
                _importResult.emit(ImportResult.Error(e.message ?: "Unknown error"))
            } finally {
                _isImporting.value = false
            }
        }
    }
}
