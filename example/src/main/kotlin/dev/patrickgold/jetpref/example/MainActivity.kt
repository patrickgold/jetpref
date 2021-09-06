package dev.patrickgold.jetpref.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.patrickgold.jetpref.datastore.preferenceModel
import dev.patrickgold.jetpref.example.ui.settings.HomeScreen
import dev.patrickgold.jetpref.example.ui.theme.JetPrefTheme

class MainActivity : ComponentActivity() {
    private val prefs by preferenceModel(::AppPrefs)

    init {
        prefs.test.isButtonShowing.observe(this) { newValue ->
            Toast.makeText(this@MainActivity, "Hello $newValue", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            JetPrefTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    AppContent(navController)
                }
            }
        }
    }
}

@Composable
fun AppContent(navController: NavHostController) {
    Column {
        TopAppBar(
            title = { Text(text = "Hello Android!") },
            backgroundColor = MaterialTheme.colors.surface
        )
        NavHost(navController = navController, startDestination = "home") {
            composable("home") { HomeScreen() }
        }
    }
}

/*@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    JetPrefTheme {
        Greeting("Android")
    }
}*/
