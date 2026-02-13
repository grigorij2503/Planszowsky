package pl.pointblank.planszowsky.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseManager @Inject constructor() {

    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    private val _aiDailyLimit = MutableStateFlow(10)
    val aiDailyLimit: StateFlow<Int> = _aiDailyLimit.asStateFlow()

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600 // Fetch every hour
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(mapOf("ai_daily_limit" to 10L))
        
        fetchRemoteConfig()
    }

    private fun fetchRemoteConfig() {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _aiDailyLimit.value = remoteConfig.getLong("ai_daily_limit").toInt()
            }
        }
    }

    // Expert Chat is now always enabled (or managed differently if needed)
    private val _isExpertChatEnabled = MutableStateFlow(true)
    val isExpertChatEnabled: StateFlow<Boolean> = _isExpertChatEnabled.asStateFlow()

    fun logError(exception: Throwable, message: String? = null) {
        message?.let { crashlytics.log(it) }
        crashlytics.recordException(exception)
    }
}