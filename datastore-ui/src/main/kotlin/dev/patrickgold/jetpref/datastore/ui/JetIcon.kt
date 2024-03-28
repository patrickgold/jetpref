package dev.patrickgold.jetpref.datastore.ui

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Interface which provides the Icon for the JetPref library
 *
 * @since 0.1.0
 */
interface JetIcon {
    /**
     * Provie the Icon.
     *
     * @param iconSpaceReserved If the space at the start of the preference should be reserved,
     *  if no icon is provided.
     *
     * @since 0.1.0
     */
    @Composable
    fun getIcon(iconSpaceReserved: Boolean): @Composable (() -> Unit)?
}

class ImageVectorIcon(private val imageVector: ImageVector?) : JetIcon {
    @Composable
    override fun getIcon(iconSpaceReserved: Boolean): @Composable (() -> Unit)? {
        return maybeJetIcon(imageVector = imageVector, iconSpaceReserved = iconSpaceReserved)
    }
}

object EmptyIcon : JetIcon {
    @Composable
    override fun getIcon(iconSpaceReserved: Boolean): @Composable (() -> Unit)? {
        return if (iconSpaceReserved) {
            ({ })
        } else {
            null
        }
    }
}

class DrawableResIcon(@DrawableRes private val iconId: Int?) : JetIcon {
    @Composable
    override fun getIcon(iconSpaceReserved: Boolean): @Composable (() -> Unit)? {
        return maybeJetIcon(id = iconId, iconSpaceReserved = iconSpaceReserved)
    }
}


/**
 * Get the JetIcon from the [ImageVector] or the [EmptyIcon] if null
 *
 * @since 0.1.0
 */
val ImageVector?.jetIcon: JetIcon
    get() = if (this == null) { EmptyIcon } else { ImageVectorIcon(this) }

/**
 * Get the JetIcon from the [DrawableRes] ID or the [EmptyIcon] if null
 *
 * @since 0.1.0
 */
val @receiver:DrawableRes Int?.jetIcon: JetIcon
    get() = if (this == null) { EmptyIcon } else { DrawableResIcon(this) }
