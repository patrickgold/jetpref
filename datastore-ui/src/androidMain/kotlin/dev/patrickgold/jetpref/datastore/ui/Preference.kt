/*
 * Copyright 2021-2026 Patrick Goldinger
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

package dev.patrickgold.jetpref.datastore.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.material.ui.JetPrefListItem

/**
 * Material list item which behaves and looks like a preference, but does not provide an
 * automatic display of state for a datastore entry, unlike all other available preference
 * composables. This can be used to manually add a custom preference or be used to allow for
 * click-only actions in a preference screen, such as navigation to a sub screen or in the app's
 * about screen.
 *
 * @param modifier Modifier to be applied to the underlying list item.
 * @param icon The [ImageVector] of the list entry icon.
 * @param iconSpaceReserved If the space at the start of the list item should be reserved (blank
 *  space) if no icon ID is provided.
 * @param overlineText The overline text of this preference, shown above the list item's primary text.
 * @param title The title of this preference, shown as the list item primary text (max 1 line).
 * @param summary The summary of this preference, shown as the list item secondary text (max 2 lines).
 * @param trailing Optional trailing composable, will be placed at the end of the list item.
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference should be
 *  enabled (true) or disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference should be
 *  visible (true) or hidden (false).
 * @param onClick The action to perform if this preference is enabled and the user clicks on this
 *  preference item.
 *
 * @since 0.1.0
 */
@Composable
fun Preference(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shapes: ListItemShapes? = null,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = LocalIconSpaceReserved.current,
    overlineText: String? = null,
    title: String,
    summary: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    if (LocalIsPrefVisible.current && visibleIf(PreferenceDataEvaluatorScope)) {
        val isEnabled = LocalIsPrefEnabled.current && enabledIf(PreferenceDataEvaluatorScope)
        CompositionLocalProvider(
            LocalIconSpaceReserved provides iconSpaceReserved,
            LocalIsPrefEnabled provides isEnabled,
            LocalIsPrefVisible provides true,
        ) {
            if (shapes != null) {
                SegmentedListItem(
                    onClick = onClick,
                    shapes = shapes,
                    modifier = modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    leadingContent = maybeJetIcon(imageVector = icon, iconSpaceReserved = iconSpaceReserved),
                    overlineContent = overlineText?.let { { Text(overlineText) } },
                    content = { Text(title) },
                    supportingContent = summary?.let { { Text(summary) } },
                    trailingContent = trailing,
                    enabled = isEnabled,
                )
            } else {
                JetPrefListItem(
                    modifier = modifier.clickable(
                        enabled = isEnabled,
                        role = Role.Button,
                        onClick = onClick,
                    ),
                    icon = maybeJetIcon(imageVector = icon, iconSpaceReserved = iconSpaceReserved),
                    overlineText = overlineText,
                    text = title,
                    secondaryText = summary,
                    trailing = trailing,
                    enabled = isEnabled,
                )
            }
        }
    }
}

/**
 * @since 0.5.0
 */
@Composable
fun Preference(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    shapes: ListItemShapes? = null,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = LocalIconSpaceReserved.current,
    overlineText: String? = null,
    title: String,
    summary: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    if (LocalIsPrefVisible.current && visibleIf(PreferenceDataEvaluatorScope)) {
        val isEnabled = LocalIsPrefEnabled.current && enabledIf(PreferenceDataEvaluatorScope)
        CompositionLocalProvider(
            LocalIconSpaceReserved provides iconSpaceReserved,
            LocalIsPrefEnabled provides isEnabled,
            LocalIsPrefVisible provides true,
        ) {
            if (shapes != null) {
                SegmentedListItem(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    shapes = shapes,
                    modifier = modifier,
                    leadingContent = maybeJetIcon(imageVector = icon, iconSpaceReserved = iconSpaceReserved),
                    overlineContent = overlineText?.let { { Text(overlineText) } },
                    content = { Text(title) },
                    supportingContent = summary?.let { { Text(summary) } },
                    trailingContent = trailing,
                    enabled = isEnabled,
                )
            } else {
                JetPrefListItem(
                    modifier = modifier.toggleable(
                        value = checked,
                        enabled = isEnabled,
                        role = Role.Switch,
                        onValueChange = onCheckedChange,
                    ),
                    icon = maybeJetIcon(imageVector = icon, iconSpaceReserved = iconSpaceReserved),
                    overlineText = overlineText,
                    text = title,
                    secondaryText = summary,
                    trailing = trailing,
                    enabled = isEnabled,
                )
            }
        }
    }
}
