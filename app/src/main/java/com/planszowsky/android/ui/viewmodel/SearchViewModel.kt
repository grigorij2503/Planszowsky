package com.planszowsky.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planszowsky.android.domain.model.Game
import com.planszowsky.android.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Game>>(emptyList())
    val searchResults: StateFlow<List<Game>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        _searchQuery
            .debounce(500)
            .distinctUntilChanged()
            .onEach { query ->
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
        _searchResults.value = repository.searchRemoteGames(query)
        _isLoading.value = false
    }

    fun addToCollection(game: Game) {
        viewModelScope.launch {
            val fullGame = repository.getRemoteGameDetails(game.id)
            if (fullGame != null) {
                repository.saveGame(fullGame)
            }
        }
    }
}