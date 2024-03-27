package dev.patrickgold.jetpref.datastore.ui

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

interface JetIcon {
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
    override fun getIcon(iconSpaceReserved: Boolean): @Composable() (() -> Unit)? {
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

val ImageVector?.jetIcon: ImageVectorIcon
    get() = ImageVectorIcon(this)

val @receiver:DrawableRes Int?.jetIcon: DrawableResIcon
    get() = DrawableResIcon(this)
