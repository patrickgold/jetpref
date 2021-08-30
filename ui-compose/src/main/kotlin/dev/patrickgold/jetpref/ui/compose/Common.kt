package dev.patrickgold.jetpref.ui.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

fun maybeJetIcon(
    @DrawableRes id: Int?,
    iconSpaceReserved: Boolean,
    contentDescription: String? = null,
): @Composable (() -> Unit)? {
    return when {
        id != null -> ({
            Icon(
                painter = painterResource(id),
                contentDescription = contentDescription,
            )
        })
        iconSpaceReserved -> ({ })
        else -> null
    }
}

@Composable
fun maybeJetText(
    text: String?
): @Composable (() -> Unit)? {
    return when {
        text != null && text.isNotBlank() -> ({ Text(text) })
        else -> null
    }
}
