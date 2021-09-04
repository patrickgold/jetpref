package dev.patrickgold.jetpref.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import dev.patrickgold.jetpref.datastore.preferenceModel
import dev.patrickgold.jetpref.example.ui.settings.HomeScreen
import dev.patrickgold.jetpref.example.ui.theme.JetPrefTheme
import dev.patrickgold.jetpref.ui.compose.SwitchPreference

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
            JetPrefTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Column(
        modifier = Modifier.verticalScroll(
            rememberScrollState()
        )
    ) {
        Text(text = "Hello $name!", style = MaterialTheme.typography.h2)
        Row {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "Icon",
                modifier = Modifier
                    .clip(CircleShape)
            )
            Text(text = "Text")
        }
        HomeScreen()
    }
}

/*@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    JetPrefTheme {
        Greeting("Android")
    }
}*/
