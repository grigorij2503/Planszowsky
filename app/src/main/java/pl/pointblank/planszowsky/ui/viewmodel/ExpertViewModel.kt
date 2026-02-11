package pl.pointblank.planszowsky.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import pl.pointblank.planszowsky.R
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.repository.UserPreferencesRepository
import pl.pointblank.planszowsky.util.FirebaseManager
import pl.pointblank.planszowsky.util.SpeechManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false
)

@HiltViewModel
class ExpertViewModel @Inject constructor(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val firebaseManager: FirebaseManager,
    private val speechManager: SpeechManager
) : AndroidViewModel(application) {

    val appTheme: StateFlow<AppTheme> = userPreferencesRepository.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.MODERN)

    val currentSpeakingId: StateFlow<String?> = speechManager.currentUtteranceId

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val aiUsageCount: StateFlow<Int> = userPreferencesRepository.aiUsageCount
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val aiDailyLimit: StateFlow<Int> = firebaseManager.aiDailyLimit
        .stateIn(viewModelScope, SharingStarted.Eagerly, 10)

    val isLimitReached: StateFlow<Boolean> = combine(aiUsageCount, aiDailyLimit) { count, limit ->
        count >= limit
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private var chatSession: com.google.firebase.ai.Chat? = null
    private var currentGameTitle: String = ""

    override fun onCleared() {
        super.onCleared()
        speechManager.stop()
    }

    fun speak(message: ChatMessage) {
        if (currentSpeakingId.value == message.id) {
            speechManager.stop()
        } else {
            speechManager.speak(message.text, message.id)
        }
    }

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
            val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel("gemini-3-flash-preview", systemInstruction = content {
                    text(app.getString(R.string.expert_system_instruction, gameTitle))
                })


            chatSession = generativeModel.startChat()
        } catch (e: Exception) {
            _messages.value += ChatMessage(
                text = app.getString(
                    R.string.expert_init_error,
                    e.localizedMessage ?: "Unknown error"
                ),
                isUser = false,
                isError = true
            )
        }
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return
        
        val app = getApplication<Application>()
        val currentCount = aiUsageCount.value
        val currentLimit = aiDailyLimit.value
        
        if (currentCount >= currentLimit) {
            _messages.value += ChatMessage(
                text = app.getString(R.string.expert_limit_reached),
                isUser = false,
                isError = true
            )
            return
        }

        // Add user message immediately
        _messages.value += ChatMessage(text = userMessage, isUser = true)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val session = chatSession
                if (session == null) {
                    _messages.value += ChatMessage(
                        text = "Sesja czatu nie jest aktywna (Błąd inicjalizacji Firebase AI).",
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
                    
                    // Increment usage count on success
                    userPreferencesRepository.incrementAiUsage(resetIfNewDay = true)
                }
            } catch (e: Exception) {
                android.util.Log.e("ExpertViewModel", "VertexAI Error", e)
                val errorMessage = if (e.localizedMessage?.contains("429") == true) {
                    "Zbyt wiele zapytań. Spróbuj ponownie za chwilę."
                } else {
                    "Błąd komunikacji: ${e.localizedMessage}"
                }

                _messages.value += ChatMessage(
                    text = errorMessage,
                    isUser = false,
                    isError = true
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}