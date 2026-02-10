package pl.pointblank.planszowsky.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseManager @Inject constructor() {

    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    // Expert Chat is now always enabled (or managed differently if needed)
    private val _isExpertChatEnabled = MutableStateFlow(true)
    val isExpertChatEnabled: StateFlow<Boolean> = _isExpertChatEnabled.asStateFlow()

    fun logError(exception: Throwable, message: String? = null) {
        message?.let { crashlytics.log(it) }
        crashlytics.recordException(exception)
    }
}