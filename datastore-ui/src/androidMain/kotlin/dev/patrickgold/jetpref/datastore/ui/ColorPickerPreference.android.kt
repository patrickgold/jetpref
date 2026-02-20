package dev.patrickgold.jetpref.datastore.ui

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun Color.safeValue(): Int {
    return if (isUnspecified && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        with(LocalContext.current) {
            resources.getColor(android.R.color.system_accent1_500, theme)
        }
    } else {
        toArgb()
    }
}

/**
 * Check if a color is the system accent color
 *
 * @param context
 * @return a boolean indicating if it is the material you color
 * @since 0.2.0
 */
fun Color.isMaterialYou(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        with(context) {
            this@isMaterialYou == Color(resources.getColor(android.R.color.system_accent1_500, theme))
        }
    } else {
        false
    }
}
