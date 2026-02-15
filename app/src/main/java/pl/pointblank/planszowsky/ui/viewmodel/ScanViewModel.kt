package pl.pointblank.planszowsky.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.pointblank.planszowsky.domain.repository.GameRepository
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState = _importState.asStateFlow()

    fun importCollection(url: String, name: String) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            val result = repository.importRemoteCollection(url, name)
            result.onSuccess { count ->
                _importState.value = ImportState.Success(count)
            }.onFailure { error ->
                _importState.value = ImportState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _importState.value = ImportState.Idle
    }

    sealed class ImportState {
        object Idle : ImportState()
        object Loading : ImportState()
        data class Success(val count: Int) : ImportState()
        data class Error(val message: String) : ImportState()
    }
}
