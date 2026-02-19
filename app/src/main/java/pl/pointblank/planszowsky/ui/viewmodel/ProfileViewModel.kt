package pl.pointblank.planszowsky.ui.viewmodel

import androidx.lifecycle.AndroidViewModel
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

import pl.pointblank.planszowsky.util.LanguageManager

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: GameRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val application: android.app.Application
) : AndroidViewModel(application) {

    val appTheme: StateFlow<AppTheme> = userPreferencesRepository.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.MODERN)
    
    val appLocale: StateFlow<String> = userPreferencesRepository.appLocale
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val bggAvatarUrl: StateFlow<String?> = userPreferencesRepository.bggAvatarUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val persistedUsername: StateFlow<String?> = userPreferencesRepository.bggUsername
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeCollectionId: StateFlow<String> = userPreferencesRepository.activeCollectionId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "main")

    val collections: StateFlow<List<pl.pointblank.planszowsky.data.local.CollectionEntity>> = repository.getAllCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @ExperimentalCoroutinesApi
    val stats: StateFlow<CollectionStats> = activeCollectionId
        .flatMapLatest { id -> repository.getCollectionStats(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CollectionStats())

    private val _bggUsername = MutableStateFlow("")
    val bggUsername: StateFlow<String> = _bggUsername.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importResult = MutableSharedFlow<ImportResult>()
    val importResult: SharedFlow<ImportResult> = _importResult.asSharedFlow()

    private var pendingImportGames: List<pl.pointblank.planszowsky.domain.model.Game> = emptyList()

    sealed class ImportResult {
        data class Success(val count: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
        data class Conflict(val count: Int) : ImportResult()
    }

    fun onUsernameChange(username: String) {
        _bggUsername.value = username
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesRepository.setAppTheme(theme)
        }
    }
    
    fun setActiveCollection(id: String) {
        viewModelScope.launch {
            userPreferencesRepository.setActiveCollectionId(id)
        }
    }

    fun refreshCollection(id: String) {
        viewModelScope.launch {
            _isImporting.value = true
            repository.refreshCollection(id)
            _isImporting.value = false
        }
    }

    fun deleteCollection(id: String) {
        viewModelScope.launch {
            repository.deleteCollection(id)
            if (activeCollectionId.value == id) {
                setActiveCollection("main")
            }
        }
    }

    fun setLocale(locale: String) {
        viewModelScope.launch {
            userPreferencesRepository.setAppLocale(locale)
            LanguageManager.applyLocale(application, locale)
        }
    }

    fun importFromBgg() {
        val username = _bggUsername.value
        if (username.isBlank()) return

        viewModelScope.launch {
            _isImporting.value = true
            try {
                // 1. Fetch User Profile (Avatar)
                val avatarUrl = repository.fetchBggUserProfile(username)
                userPreferencesRepository.setBggAvatarUrl(avatarUrl)
                userPreferencesRepository.setBggUsername(username)
                
                // 2. Fetch Collection
                val fetchedGames = repository.fetchCollection(username)
                
                // 3. Detect Conflicts
                val existingIds = mutableSetOf<String>()
                // We need to check existence. Repo already has getGame(id).
                // But checking 600 games one by one is slow.
                // However, we don't have a batch check in DAO yet.
                // Let's assume for now we check against currently saved games list.
                // We can't easily get ALL IDs from repo as Flow without collecting.
                
                // Alternative: Repo.saveImportedGames returns the count of NEW games.
                // But we want to ASK before saving.
                
                // Let's add a quick check:
                var conflictCount = 0
                fetchedGames.forEach { 
                    if (repository.getGame(it.id) != null) conflictCount++
                }

                if (conflictCount > 0) {
                    pendingImportGames = fetchedGames
                    _importResult.emit(ImportResult.Conflict(conflictCount))
                } else {
                    val count = repository.saveImportedGames(fetchedGames, overwriteExisting = false)
                    _importResult.emit(ImportResult.Success(count))
                }
            } catch (e: Exception) {
                _importResult.emit(ImportResult.Error(e.message ?: "Unknown error"))
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun confirmImport(overwriteExisting: Boolean) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val count = repository.saveImportedGames(pendingImportGames, overwriteExisting)
                _importResult.emit(ImportResult.Success(count))
                pendingImportGames = emptyList()
            } catch (e: Exception) {
                _importResult.emit(ImportResult.Error(e.message ?: "Unknown error"))
            } finally {
                _isImporting.value = false
            }
        }
    }

    suspend fun exportCollection(): String {
        return repository.exportCollectionToJson(activeCollectionId.value)
    }

    suspend fun exportCollectionCsv(): String {
        return repository.exportCollectionToCsv(activeCollectionId.value)
    }

    fun startCsvImport(csv: String) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val fetchedGames = repository.parseCsv(csv)
                if (fetchedGames.isEmpty()) {
                    _isImporting.value = false
                    return@launch
                }

                var conflictCount = 0
                fetchedGames.forEach { 
                    if (repository.getGame(it.id) != null) conflictCount++
                }

                if (conflictCount > 0) {
                    pendingImportGames = fetchedGames
                    _importResult.emit(ImportResult.Conflict(conflictCount))
                } else {
                    val count = repository.saveImportedGames(fetchedGames, overwriteExisting = false)
                    _importResult.emit(ImportResult.Success(count))
                }
            } catch (e: Exception) {
                _importResult.emit(ImportResult.Error(e.message ?: "Unknown error"))
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun resetImportResult() {
        viewModelScope.launch {
            // SharedFlow doesn't have a reset, but we can emit a null-like state if needed.
            // Or just rely on the UI state.
        }
    }
}
