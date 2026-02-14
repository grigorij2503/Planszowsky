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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.domain.repository.UserPreferencesRepository
import pl.pointblank.planszowsky.ui.PlanszowskyMainContainer
import pl.pointblank.planszowsky.ui.theme.PlanszowskyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import pl.pointblank.planszowsky.util.LanguageManager

import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MainActivityEntryPoint {
        fun userPreferencesRepository(): UserPreferencesRepository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        
        // Apply saved language preference using EntryPoint to avoid injection timing issues
        runBlocking {
            val entryPoint = EntryPointAccessors.fromApplication(applicationContext, MainActivityEntryPoint::class.java)
            val userPreferencesRepository = entryPoint.userPreferencesRepository()
            
            val locale = userPreferencesRepository.appLocale.first()
            if (locale != "system") {
                LanguageManager.applyLocale(this@MainActivity, locale)
            }
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            // We can still use the entry point here or inject a ViewModel that holds it.
            // But for the theme, we need the repo again. 
            // Since we removed the field, let's get it from the entry point again or let Hilt inject into the ViewModel.
            // For the theme flow, we need the repo instance.
            
            val entryPoint = EntryPointAccessors.fromApplication(applicationContext, MainActivityEntryPoint::class.java)
            val userPreferencesRepository = entryPoint.userPreferencesRepository()
            
            val appTheme by userPreferencesRepository.appTheme.collectAsState(initial = AppTheme.MODERN)
            
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
