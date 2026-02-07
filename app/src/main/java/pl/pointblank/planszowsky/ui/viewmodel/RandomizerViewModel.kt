package pl.pointblank.planszowsky.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pl.pointblank.planszowsky.domain.model.Game
import pl.pointblank.planszowsky.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.repository.UserPreferencesRepository

import kotlinx.coroutines.flow.combine

enum class DurationFilter { ANY, SHORT, MEDIUM, LONG }

@HiltViewModel
class RandomizerViewModel @Inject constructor(
    private val repository: GameRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = userPreferencesRepository.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.MODERN)

    private val _playerFilter = MutableStateFlow<Int?>(null)
    val playerFilter: StateFlow<Int?> = _playerFilter.asStateFlow()

    private val _durationFilter = MutableStateFlow(DurationFilter.ANY)
    val durationFilter: StateFlow<DurationFilter> = _durationFilter.asStateFlow()

    val games: StateFlow<List<Game>> = repository.getSavedGames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredGames: StateFlow<List<Game>> = combine(
        games,
        _playerFilter,
        _durationFilter
    ) { allGames, players, duration ->
        allGames.filter { game ->
            val matchesPlayers = players == null || (
                (game.minPlayers?.toIntOrNull() ?: 0) <= players && 
                (game.maxPlayers?.toIntOrNull() ?: 99) >= players
            )
            
            val time = game.playingTime?.toIntOrNull() ?: 0
            val matchesDuration = when (duration) {
                DurationFilter.ANY -> true
                DurationFilter.SHORT -> time > 0 && time <= 45
                DurationFilter.MEDIUM -> time > 45 && time <= 90
                DurationFilter.LONG -> time > 90
            }
            
            matchesPlayers && matchesDuration
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedGame = MutableStateFlow<Game?>(null)
    val selectedGame: StateFlow<Game?> = _selectedGame.asStateFlow()

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    fun setPlayerFilter(count: Int?) {
        _playerFilter.value = count
    }

    fun setDurationFilter(filter: DurationFilter) {
        _durationFilter.value = filter
    }

    fun spin() {
        val currentGames = filteredGames.value
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
