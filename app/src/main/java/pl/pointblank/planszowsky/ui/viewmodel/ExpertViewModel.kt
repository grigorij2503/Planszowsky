package pl.pointblank.planszowsky.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import pl.pointblank.planszowsky.BuildConfig
import pl.pointblank.planszowsky.R
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false
)

@HiltViewModel
class ExpertViewModel @Inject constructor(
    application: Application,
    userPreferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {

    val appTheme: StateFlow<AppTheme> = userPreferencesRepository.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.MODERN)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var chatSession: com.google.ai.client.generativeai.Chat? = null
    private var currentGameTitle: String = ""

    fun initialize(gameTitle: String) {
        if (currentGameTitle == gameTitle && chatSession != null) return
        
        currentGameTitle = gameTitle
        val app = getApplication<Application>()
        _messages.value = listOf(
            ChatMessage(
                text = app.getString(R.string.expert_welcome, gameTitle),
                isUser = false
            )
        )

        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank()) {
                _messages.value += ChatMessage(
                                    text = app.getString(R.string.expert_api_error),
                                    isUser = false,
                                    isError = true
                                )
                return
            }

            val generativeModel = GenerativeModel(
                modelName = "gemini-3-flash-preview",
                apiKey = apiKey,
                systemInstruction = content { 
                    text(app.getString(R.string.expert_system_instruction, gameTitle)) 
                }
            )
            
            chatSession = generativeModel.startChat()
        } catch (e: Exception) {
            _messages.value += ChatMessage(
                            text = app.getString(R.string.expert_init_error, e.localizedMessage ?: "Unknown error"),
                            isUser = false,
                            isError = true
                        )
        }
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        // Add user message immediately
        _messages.value += ChatMessage(text = userMessage, isUser = true)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val session = chatSession
                if (session == null) {
                    _messages.value += ChatMessage(
                                            text = "Sesja czatu nie jest aktywna (brak klucza API?).",
                                            isUser = false,
                                            isError = true
                                        )
                } else {
                    val response = session.sendMessage(userMessage)
                    val aiResponseText = response.text ?: "Brak odpowiedzi od modelu."

                    _messages.value += ChatMessage(
                                            text = aiResponseText,
                                            isUser = false
                                        )
                }
            } catch (e: Exception) {
                android.util.Log.e("ExpertViewModel", "Gemini Error", e)
                _messages.value += ChatMessage(
                                    text = "Błąd komunikacji: ${e.localizedMessage}",
                                    isUser = false,
                                    isError = true
                                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
