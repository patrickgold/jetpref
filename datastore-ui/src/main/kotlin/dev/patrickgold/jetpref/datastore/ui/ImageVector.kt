package dev.patrickgold.jetpref.datastore.ui

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource

/**
 * Retrieves the [ImageVector] corresponding to the provided drawable resource ID.
 *
 * @param id The resource ID of the drawable.
 * @return The [ImageVector] corresponding to the provided resource ID, or null if an error occurs.
 *
 * @since 0.1.0
 */
fun Context.vectorResource(@DrawableRes id: Int): ImageVector? {
    val theme = this.theme
    return try {
        ImageVector.vectorResource(theme = theme, resId = id, res = this.resources)
    } catch (_: Exception) {
        null
    }
}

/**
 * Retrieves the [ImageVector] corresponding to the provided drawable resource ID within the current Composable context.
 *
 * @param id The resource ID of the drawable.
 * @return The [ImageVector] corresponding to the provided resource ID, or null if an error occurs.
 *
 * @since 0.1.0
 */
@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun vectorResource(@DrawableRes id: Int): ImageVector? {
    val context = LocalContext.current
    return context.vectorResource(id)
}
