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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import dev.patrickgold.jetpref.datastore.component.PreferenceScreen
import dev.patrickgold.jetpref.datastore.model.collectAsState
import dev.patrickgold.jetpref.datastore.ui.JetPrefHost
import dev.patrickgold.jetpref.datastore.ui.PreferenceNavigationRouter
import dev.patrickgold.jetpref.datastore.ui.ProvideDialogPrefStrings
import dev.patrickgold.jetpref.example.ui.settings.HomeScreen
import dev.patrickgold.jetpref.example.ui.theme.JetPrefTheme
import dev.patrickgold.jetpref.example.ui.theme.Theme

class MainActivity : ComponentActivity() {
    private val prefs by ExamplePreferenceStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val appTheme by prefs.theme.collectAsState()
            val isDark = when (appTheme) {
                Theme.AUTO -> isSystemInDarkTheme()
                Theme.LIGHT -> false
                Theme.DARK -> true
            }
            JetPrefTheme(isDark) {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppContent()
                }
            }
        }
    }
}

data class ExampleRoute(
    val screen: PreferenceScreen,
    val componentId: Int = -1,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent() {
    ProvideDialogPrefStrings(
        confirmLabel = "OK",
        dismissLabel = "Cancel",
        neutralLabel = "Default",
    ) {
        Column {
            TopAppBar(
                title = { Text(text = "Example JetPref App") },
                colors = TopAppBarDefaults.topAppBarColors()
            )
            val backStack = remember { mutableStateListOf(ExampleRoute(HomeScreen)) }
            val router = remember {
                PreferenceNavigationRouter { screen, item ->
                    val route = ExampleRoute(screen, componentId = item?.id ?: -1)
                    backStack.add(route)
                }
            }
            JetPrefHost(router) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = { route ->
                        NavEntry(route) {
                            route.screen(route.componentId)
                        }
                    },
                )
            }
        }
    }
}
