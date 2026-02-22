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

package dev.patrickgold.jetpref.material.ui

import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp

/**
 * Material Design list item.
 *
 * @param modifier Modifier to be applied to the list item.
 * @param icon The leading supporting visual of the list item.
 * @param overlineText The text displayed above the primary text.
 * @param text The primary text of the list item.
 * @param secondaryText The secondary text of the list item.
 * @param singleLineSecondaryText If the secondary text should be limited to a single line.
 * @param enabled If false, this list item will be grayed out.
 * @param trailingContent The trailing meta text, icon, switch or checkbox.
 * @param colors The colors to be used for the list item.
 * @param tonalElevation The elevation to be used for the tonal surface.
 * @param shadowElevation The elevation to be used for the shadow surface.
 *
 * @since 0.1.0
 *
 * @see androidx.compose.material3.ListItem
 */
@Composable
fun JetPrefListItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    overlineText: String? = null,
    text: String,
    secondaryText: String? = null,
    singleLineSecondaryText: Boolean = false,
    enabled: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
) {
    JetPrefListItem(
        modifier,
        icon,
        overlineContent = whenNotNullOrBlank(overlineText) { str ->
            Text(
                text = str,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        headlineContent = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        secondaryContent = whenNotNullOrBlank(secondaryText) { str ->
            Text(
                text = str,
                maxLines = if (singleLineSecondaryText) { 1 } else { 2 },
                overflow = TextOverflow.Ellipsis,
            )
        },
        enabled,
        trailingContent = trailingContent,
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    )
}

/**
 * Material Design list item.
 *
 * @param modifier Modifier to be applied to the list item.
 * @param icon The leading supporting visual of the list item.
 * @param overlineContent The text displayed above the primary text.
 * @param headlineContent The primary text of the list item.
 * @param secondaryContent The secondary composable of the list item.
 * @param enabled If false, this list item will be grayed out.
 * @param trailingContent The trailing meta text, icon, switch or checkbox.
 * @param colors The colors to be used for the list item.
 * @param tonalElevation The elevation to be used for the tonal surface.
 * @param shadowElevation The elevation to be used for the shadow surface.
 *
 * @since 0.4.0
 *
 * @see androidx.compose.material3.ListItem
 */
@Composable
fun JetPrefListItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    overlineContent: (@Composable () -> Unit)? = null,
    headlineContent: (@Composable () -> Unit),
    secondaryContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
) {
    ListItem(
        modifier = modifier.alpha(if (enabled) 1.0f else 0.38f),
        leadingContent = icon,
        overlineContent = overlineContent,
        headlineContent = headlineContent,
        supportingContent = secondaryContent,
        trailingContent = trailingContent,
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    )
}
