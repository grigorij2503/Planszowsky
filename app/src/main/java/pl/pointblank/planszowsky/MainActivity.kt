package pl.pointblank.planszowsky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.repository.UserPreferencesRepository
import pl.pointblank.planszowsky.ui.PlanszowskyMainContainer
import pl.pointblank.planszowsky.ui.theme.PlanszowskyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pl.pointblank.planszowsky.util.LanguageManager
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Volatile
    private var isLocaleReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isLocaleReady }

        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            try {
                val locale = userPreferencesRepository.appLocale.first()
                if (LanguageManager.getCurrentLocale(this@MainActivity) != locale) {
                    LanguageManager.applyLocale(this@MainActivity, locale)
                }
            } finally {
                isLocaleReady = true
            }
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            val appTheme by userPreferencesRepository.appTheme.collectAsStateWithLifecycle(initialValue = AppTheme.MODERN)

            var showCustomSplash by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }

            if (showCustomSplash) {
                pl.pointblank.planszowsky.ui.screens.PointBlankSplashScreen(
                    onAnimationFinished = {
                        showCustomSplash = false
                    }
                )
            } else {
                PlanszowskyTheme(appTheme = appTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        PlanszowskyMainContainer(appTheme = appTheme)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MaterialTheme {
        Greeting("Android")
    }
}
