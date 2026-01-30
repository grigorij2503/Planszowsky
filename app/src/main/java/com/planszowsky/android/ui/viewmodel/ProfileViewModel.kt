package com.planszowsky.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planszowsky.android.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _bggUsername = MutableStateFlow("")
    val bggUsername: StateFlow<String> = _bggUsername.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    fun onUsernameChange(username: String) {
        _bggUsername.value = username
    }

    fun importFromBgg() {
        val username = _bggUsername.value
        if (username.isBlank()) return

        viewModelScope.launch {
            _isImporting.value = true
            // In a real scenario, we'd fetch the collection from BGG.
            // For now, we'll simulate a small delay.
            kotlinx.coroutines.delay(2000)
            
            // To be implemented: actual fetch logic using repository.importCollection(username)
            
            _isImporting.value = false
        }
    }
}
