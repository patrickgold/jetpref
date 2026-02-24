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
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dev.patrickgold.jetpref.datastore.model.collectAsState
import dev.patrickgold.jetpref.datastore.ui.JetPrefHost
import dev.patrickgold.jetpref.datastore.ui.PreferenceNavigationRouter
import dev.patrickgold.jetpref.datastore.ui.ProvideDialogPrefStrings
import dev.patrickgold.jetpref.example.ui.settings.ColorPickerDemoScreen
import dev.patrickgold.jetpref.example.ui.settings.HomePage
import dev.patrickgold.jetpref.example.ui.settings.ParameterizedScreen
import dev.patrickgold.jetpref.example.ui.settings.SearchScreen
import dev.patrickgold.jetpref.example.ui.settings.SubPage
import dev.patrickgold.jetpref.example.ui.settings.VisualizeSearchIndexScreen
import dev.patrickgold.jetpref.example.ui.theme.JetPrefTheme
import dev.patrickgold.jetpref.example.ui.theme.Theme
import kotlinx.serialization.Serializable

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

sealed interface Route : NavKey {
    @Serializable
    data object ColorPickerDemo : Route

    @Serializable
    data class Home(
        val anchorId: Int = -1,
    ) : Route

    @Serializable
    data class Parameterized(
        val name: String,
    ) : Route

    @Serializable
    data object Search : Route

    @Serializable
    data class Sub(
        val anchorId: Int = -1,
    ) : Route

    @Serializable
    data object VisualizeSearchIndex : Route
}

class Navigator(val state: NavBackStack<NavKey>) {
    fun navigateTo(navKey: NavKey) {
        state.add(navKey)
    }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("No Navigator provided.") }

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
            val backStack = rememberNavBackStack(Route.Home())
            val navigator = remember { Navigator(backStack) }
            val router = remember {
                PreferenceNavigationRouter { page, anchor ->
                    val anchorId = anchor?.id ?: -1
                    when (page) {
                        is HomePage -> navigator.navigateTo(Route.Home(anchorId))
                        is SubPage -> navigator.navigateTo(Route.Sub(anchorId))
                    }
                }
            }
            CompositionLocalProvider(LocalNavigator provides navigator) {
                JetPrefHost(router) {
                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryProvider = entryProvider {
                            entry<Route.Home> { route ->
                                HomePage(anchorId = route.anchorId)
                            }
                            entry<Route.Sub> { route ->
                                SubPage(anchorId = route.anchorId)
                            }
                            entry<Route.ColorPickerDemo> {
                                ColorPickerDemoScreen()
                            }
                            entry<Route.Parameterized> { route ->
                                ParameterizedScreen(route.name)
                            }
                            entry<Route.Search> {
                                SearchScreen()
                            }
                            entry<Route.VisualizeSearchIndex> {
                                VisualizeSearchIndexScreen()
                            }
                        },
                    )
                }
            }
        }
    }
}
