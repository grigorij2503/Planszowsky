package com.planszowsky.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planszowsky.android.domain.model.Game
import com.planszowsky.android.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class RandomizerViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    val games: StateFlow<List<Game>> = repository.getSavedGames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedGame = MutableStateFlow<Game?>(null)
    val selectedGame: StateFlow<Game?> = _selectedGame.asStateFlow()

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    fun spin() {
        val currentGames = games.value
        if (currentGames.isEmpty()) return

        viewModelScope.launch {
            _isSpinning.value = true
            // Simulate spinning delay
            kotlinx.coroutines.delay(2000)
            _selectedGame.value = currentGames[Random.nextInt(currentGames.size)]
            _isSpinning.value = false
        }
    }
}
