package dev.patrickgold.jetpref.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.example.ui.settings.HomeScreen
import dev.patrickgold.jetpref.example.ui.theme.JetPrefTheme
import dev.patrickgold.jetpref.ui.compose.ProvideDefaultDialogPrefStrings

class MainActivity : ComponentActivity() {
    private val prefs by examplePreferenceModel()

    init {
        prefs.example.isButtonShowing.observe(this) { newValue ->
            Toast.makeText(this@MainActivity, "Value $newValue", Toast.LENGTH_SHORT).show()
        }
    }

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
            JetPrefTheme(isDark) {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    AppContent(navController)
                }
            }
        }
    }

    override fun onDestroy() {
        prefs.forceSyncToDisk()
        super.onDestroy()
    }
}

@Composable
fun AppContent(navController: NavHostController) {
    ProvideDefaultDialogPrefStrings(
        confirmLabel = "Confirm",
        dismissLabel = "Dismiss",
        neutralLabel = "Def. value",
    ) {
        Column {
            TopAppBar(
                title = { Text(text = "Example JetPref App") },
                backgroundColor = MaterialTheme.colors.surface
            )
            NavHost(navController = navController, startDestination = "home") {
                composable("home") { HomeScreen() }
            }
        }
    }
}
