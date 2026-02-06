package pl.pointblank.planszowsky.util

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseManager @Inject constructor() {

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    private val analytics: FirebaseAnalytics = Firebase.analytics

    private val _isExpertChatEnabled = MutableStateFlow(false)
    val isExpertChatEnabled: StateFlow<Boolean> = _isExpertChatEnabled.asStateFlow()

    private val _installationId = MutableStateFlow<String?>(null)
    val installationId: StateFlow<String?> = _installationId.asStateFlow()

    init {
        setupRemoteConfig()
        fetchInstallId()
    }

            private fun setupRemoteConfig() {
                val configSettings = remoteConfigSettings {
                    minimumFetchIntervalInSeconds = 3600 // 1 hour in production
                }
                remoteConfig.setConfigSettingsAsync(configSettings)        // Default values
        val defaults = mapOf(
            "is_expert_chat_enabled" to false
        )
        remoteConfig.setDefaultsAsync(defaults)

        // Fetch and activate
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _isExpertChatEnabled.value = remoteConfig.getBoolean("is_expert_chat_enabled")
            }
        }
    }

    private fun fetchInstallId() {
        FirebaseInstallations.getInstance().id.addOnSuccessListener { id ->
            _installationId.value = id
            // Set as user property so we can target this specific device in Firebase Console
            analytics.setUserProperty("installation_id", id)
            android.util.Log.d("FirebaseManager", "Installation ID: $id")
        }
    }

    fun refreshConfig() {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _isExpertChatEnabled.value = remoteConfig.getBoolean("is_expert_chat_enabled")
            }
        }
    }
}
