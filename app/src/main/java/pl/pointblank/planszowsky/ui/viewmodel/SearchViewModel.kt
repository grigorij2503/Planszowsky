package pl.pointblank.planszowsky.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.model.Game
import pl.pointblank.planszowsky.domain.repository.GameRepository
import pl.pointblank.planszowsky.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import pl.pointblank.planszowsky.util.similarity

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: GameRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = userPreferencesRepository.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.MODERN)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Game>>(emptyList())
    val searchResults: StateFlow<List<Game>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _additionSuccess = MutableStateFlow(false)
    val additionSuccess: StateFlow<Boolean> = _additionSuccess.asStateFlow()

    init {
        _searchQuery
            .debounce(1000)
            .distinctUntilChanged()
            .onEach { query ->
                _error.value = null
                if (query.isNotBlank()) {
                    performSearch(query)
                } else {
                    _searchResults.value = emptyList()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    private suspend fun performSearch(query: String) {
        _isLoading.value = true
        _error.value = null
        val cleanQuery = query.trim()
        
        try {
            val results = if (cleanQuery.all { it.isDigit() } && cleanQuery.length >= 8) {
                repository.searchByBarcode(cleanQuery)
            } else {
                repository.searchRemoteGames(cleanQuery)
            }
            
            _searchResults.value = rankResults(cleanQuery, results)
        } catch (e: Exception) {
            _error.value = "api_error"
            _searchResults.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    private fun rankResults(query: String, results: List<Game>): List<Game> {
        if (query.isBlank()) return results
        
        return results.sortedByDescending { game ->
            val title = game.title.lowercase()
            val q = query.lowercase()
            
            when {
                title == q -> 1000.0 // Exact match
                title.startsWith(q) -> 800.0 // Starts with
                title.contains(q) -> 500.0 // Contains
                else -> title.similarity(q) * 100.0 // Fuzzy similarity
            }
        }
    }

    fun addToCollection(game: Game) {
        viewModelScope.launch {
            _isLoading.value = true
            val fullGame = repository.getRemoteGameDetails(game.id)
            if (fullGame != null) {
                repository.saveGame(fullGame)
                _additionSuccess.value = true
            }
            _isLoading.value = false
        }
    }
}
