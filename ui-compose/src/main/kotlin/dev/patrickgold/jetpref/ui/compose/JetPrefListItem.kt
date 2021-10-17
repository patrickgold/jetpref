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

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun JetPrefListItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    overlineText: String? = null,
    text: String,
    secondaryText: String? = null,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = modifier.alpha(if (enabled) ContentAlpha.high else ContentAlpha.disabled),
        icon = icon,
        overlineText = whenNotNullOrBlank(overlineText) { str ->
            Text(
                text = str,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        secondaryText = whenNotNullOrBlank(secondaryText) { str ->
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = str,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailing = trailing,
    )
}
