package dev.patrickgold.jetpref.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.datastore.ui.ProvideDefaultDialogPrefStrings
import dev.patrickgold.jetpref.example.ui.settings.ColorPickerDemoScreen
import dev.patrickgold.jetpref.example.ui.settings.HomeScreen
import dev.patrickgold.jetpref.example.ui.theme.JetPrefTheme
import dev.patrickgold.jetpref.example.ui.theme.Theme

val LocalNavController = staticCompositionLocalOf<NavHostController> { error("not init") }

class MainActivity : ComponentActivity() {
    private val prefs by examplePreferenceModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            val appTheme by prefs.theme.observeAsState()
            val isDark = when (appTheme) {
                Theme.AUTO -> isSystemInDarkTheme()
                Theme.LIGHT -> false
                Theme.DARK -> true
            }
            CompositionLocalProvider(LocalNavController provides navController) {
                JetPrefTheme(isDark) {
                    // A surface container using the 'background' color from the theme
                    Surface(color = MaterialTheme.colorScheme.background) {
                        AppContent(navController)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(navController: NavHostController) {
    ProvideDefaultDialogPrefStrings(
        confirmLabel = "OK",
        dismissLabel = "Cancel",
        neutralLabel = "Default",
    ) {
        Column {
            TopAppBar(
                title = { Text(text = "Example JetPref App") },
                colors = TopAppBarDefaults.topAppBarColors()
            )
            NavHost(navController = navController, startDestination = "home") {
                composable("home") { HomeScreen() }
                composable("color-picker-demo") { ColorPickerDemoScreen() }
            }
        }
    }
}
