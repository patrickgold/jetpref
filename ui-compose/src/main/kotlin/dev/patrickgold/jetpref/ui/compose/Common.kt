/*
 * Copyright 2021 Patrick Goldinger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.jetpref.ui.compose

import androidx.annotation.DrawableRes
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow

@Composable
internal fun maybeJetIcon(
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
internal inline fun whenNotNullOrBlank(
    string: String?,
    crossinline composer: @Composable (text: String) -> Unit,
): @Composable (() -> Unit)? {
    return when {
        string != null && string.isNotBlank() -> ({ composer(string) })
        else -> null
    }
}

internal fun String.formatValue(value: Any?): String {
    return this.replace("{v}", value.toString())
}
