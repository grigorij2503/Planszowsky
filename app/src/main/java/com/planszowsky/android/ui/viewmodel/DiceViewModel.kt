package com.planszowsky.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.UUID
import javax.inject.Inject

enum class DiceMode {
    ONE_D6, TWO_D6, CUSTOM
}

data class DieState(
    val id: String = UUID.randomUUID().toString(),
    val sides: Int,
    val value: Int,
    val rotation: Float = 0f
)

data class DiceUiState(
    val mode: DiceMode = DiceMode.ONE_D6,
    val dice: List<DieState> = emptyList(),
    val isRolling: Boolean = false,
    val totalSum: Int = 0,
    val customDiceCount: Int = 1,
    val customDiceSides: Int = 20
)

@HiltViewModel
class DiceViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DiceUiState())
    val uiState: StateFlow<DiceUiState> = _uiState.asStateFlow()

    private val random = SecureRandom()

    init {
        setupDiceForMode(DiceMode.ONE_D6)
    }

    fun onModeSelected(mode: DiceMode) {
        _uiState.update { it.copy(mode = mode) }
        setupDiceForMode(mode)
    }

    fun onCustomDiceConfigChanged(count: Int, sides: Int) {
        _uiState.update { it.copy(customDiceCount = count, customDiceSides = sides) }
        if (_uiState.value.mode == DiceMode.CUSTOM) {
            setupDiceForMode(DiceMode.CUSTOM)
        }
    }

    private fun setupDiceForMode(mode: DiceMode) {
        val currentState = _uiState.value
        val newDice = when (mode) {
            DiceMode.ONE_D6 -> List(1) { createDie(6) }
            DiceMode.TWO_D6 -> List(2) { createDie(6) }
            DiceMode.CUSTOM -> List(currentState.customDiceCount) { createDie(currentState.customDiceSides) }
        }
        _uiState.update {
            it.copy(
                dice = newDice,
                totalSum = newDice.sumOf { die -> die.value },
                isRolling = false
            )
        }
    }

    private fun createDie(sides: Int): DieState {
        return DieState(
            sides = sides,
            value = 1,
            rotation = 0f
        )
    }

    fun rollDice() {
        if (_uiState.value.isRolling) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRolling = true) }

            val rollDuration = 600L
            val startTime = System.currentTimeMillis()
            
            while (System.currentTimeMillis() - startTime < rollDuration) {
                generateRandomDiceValues()
                delay(80)
            }

            generateRandomDiceValues(finalize = true)
            _uiState.update { it.copy(isRolling = false) }
        }
    }

    private fun generateRandomDiceValues(finalize: Boolean = false) {
        _uiState.update { state ->
            val newDice = state.dice.map { die ->
                die.copy(
                    value = random.nextInt(die.sides) + 1,
                    rotation = if (finalize) (random.nextFloat() * 30f - 15f) else (random.nextFloat() * 360f)
                )
            }
            state.copy(
                dice = newDice,
                totalSum = newDice.sumOf { it.value }
            )
        }
    }
}
