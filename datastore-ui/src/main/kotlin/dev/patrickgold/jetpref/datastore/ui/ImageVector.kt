package dev.patrickgold.jetpref.datastore.ui

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource


fun Context.vectorResource(@DrawableRes id: Int): ImageVector? {
    val theme = this.theme
    return try {
        ImageVector.vectorResource(theme = theme, resId = id, res = this.resources)
    } catch (_: Exception) {
        null
    }
}

@Composable
inline fun vectorResource(@DrawableRes id: Int): ImageVector? {
    val context = LocalContext.current
    return context.vectorResource(id)
}
