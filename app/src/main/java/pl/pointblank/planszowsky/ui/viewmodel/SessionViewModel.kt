package pl.pointblank.planszowsky.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.pointblank.planszowsky.data.local.PlayerScore
import pl.pointblank.planszowsky.data.local.SessionEntity
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.repository.GameRepository
import pl.pointblank.planszowsky.domain.repository.UserPreferencesRepository
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val repository: GameRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val appTheme: StateFlow<AppTheme> = userPreferencesRepository.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.MODERN)

    val activeSession: StateFlow<SessionEntity?> = repository.getActiveSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun startSession(gameId: String, gameTitle: String, playerNames: List<String>) {
        viewModelScope.launch {
            repository.clearActiveSessions()
            val colors = listOf(0xFFE91E63.toInt(), 0xFF2196F3.toInt(), 0xFF4CAF50.toInt(), 0xFFFFEB3B.toInt(), 0xFFFF9800.toInt(), 0xFF9C27B0.toInt())
            val players = playerNames.mapIndexed { index, name ->
                PlayerScore(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    score = 0,
                    color = colors[index % colors.size]
                )
            }
            val newSession = SessionEntity(
                gameId = gameId,
                gameTitle = gameTitle,
                startTime = System.currentTimeMillis(),
                players = players,
                isActive = true
            )
            repository.saveSession(newSession)
        }
    }

    fun updateScore(playerId: String, delta: Int) {
        viewModelScope.launch {
            val current = activeSession.value ?: return@launch
            val updatedPlayers = current.players.map {
                if (it.id == playerId) it.copy(score = it.score + delta) else it
            }
            repository.saveSession(current.copy(players = updatedPlayers))
        }
    }

    fun updateNotes(notes: String) {
        viewModelScope.launch {
            val current = activeSession.value ?: return@launch
            repository.saveSession(current.copy(notes = notes))
        }
    }

    fun endSession() {
        viewModelScope.launch {
            activeSession.value?.let {
                repository.deleteSession(it.gameId)
            }
        }
    }
}
