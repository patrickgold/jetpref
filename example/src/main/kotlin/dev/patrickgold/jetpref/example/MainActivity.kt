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
import dev.patrickgold.jetpref.datastore.model.collectAsState
import dev.patrickgold.jetpref.datastore.ui.JetPrefHost
import dev.patrickgold.jetpref.datastore.ui.PreferenceNavigationRouter
import dev.patrickgold.jetpref.datastore.ui.PreferenceScreen
import dev.patrickgold.jetpref.datastore.ui.ProvideDialogPrefStrings
import dev.patrickgold.jetpref.example.ui.settings.ColorPickerDemoScreen
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

sealed interface ExampleRoutes {
    val componentId: Int

    data class Home(override val componentId: Int = -1) : ExampleRoutes
    data class ColorPickerDemo(override val componentId: Int = -1) : ExampleRoutes
}

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
            val backStack = remember { mutableStateListOf<ExampleRoutes>(ExampleRoutes.Home()) }
            val router = remember {
                PreferenceNavigationRouter { screen, item ->
                    val route = when (screen) {
                        HomeScreen -> ExampleRoutes.Home(item?.id ?: -1)
                        ColorPickerDemoScreen -> ExampleRoutes.ColorPickerDemo(item?.id ?: -1)
                        else -> error("unknown route $screen $item")
                    }
                    backStack.add(route)
                }
            }
            JetPrefHost(router) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = { route ->
                        when (route) {
                            is ExampleRoutes.Home -> NavEntry(route) {
                                PreferenceScreen(HomeScreen, route.componentId)
                            }
                            is ExampleRoutes.ColorPickerDemo -> NavEntry(route) {
                                ColorPickerDemoScreen()
                            }
                        }
                    }
                )
            }
        }
    }
}

