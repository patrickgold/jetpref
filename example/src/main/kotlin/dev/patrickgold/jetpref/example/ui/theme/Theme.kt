package dev.patrickgold.jetpref.example.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.platform.LocalContext
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.datastore.ui.listPrefEntries
import dev.patrickgold.jetpref.example.examplePreferenceModel

private val DarkColorPalette = darkColorScheme(
    primary = Purple200,
    secondary = Purple700,
    tertiary = Teal200
)

private val LightColorPalette = lightColorScheme(
    primary = Purple500,
    secondary = Purple700,
    tertiary = Teal200

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
private fun colors(darkTheme: Boolean) = if (darkTheme) {
    DarkColorPalette
} else {
    LightColorPalette
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun dynamicColors(darkTheme: Boolean) = if (darkTheme) {
    dynamicDarkColorScheme(LocalContext.current)
} else {
    dynamicLightColorScheme(LocalContext.current)
}

@Composable
fun JetPrefTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {

    val prefs by examplePreferenceModel()
    val accentColor = prefs.accentColor.observeAsState()

    val dynamicColors = accentColor.value.isUnspecified

    val colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dynamicColors) {
        dynamicColors(darkTheme = darkTheme)
    } else {
        colors(darkTheme = darkTheme)
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

enum class Theme {
    AUTO,
    LIGHT,
    DARK;

    companion object {
        @Composable
        fun listEntries() = listPrefEntries {
            entry(
                key = AUTO,
                label = "System default",
            )
            entry(
                key = LIGHT,
                label = "Light",
            )
            entry(
                key = DARK,
                label = "Dark",
            )
        }
    }
}
