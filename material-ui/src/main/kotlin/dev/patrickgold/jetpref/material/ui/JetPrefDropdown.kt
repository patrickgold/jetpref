/*
 * Copyright 2025 Patrick Goldinger
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

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

/**
 * A dropdown that allows the user to select an option from a list of options.
 *
 * @param options The list of options to choose from.
 * @param selectedOptionIndex The index of the currently selected option.
 * @param onSelectOption The callback that is called when an option is selected.
 * @param modifier The modifier to apply to the layout.
 * @param expanded The state of the dropdown menu.
 * @param enabled Whether the dropdown menu is enabled.
 * @param isError Whether the dropdown menu is in an error state.
 * @param labelText The text to display as the label of the dropdown menu. Ignored if
 *  [label] is provided.
 * @param label The composable to display as the label of the dropdown menu.
 * @param optionsLabelProvider A function that provides a label for each option. If not
 *  provided, the options will be converted to strings using `toString`.
 * @param appearance The appearance of the dropdown menu.
 *
 * @since 0.2.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> JetPrefDropdown(
    options: List<T>,
    selectedOptionIndex: Int,
    onSelectOption: (Int) -> Unit,
    modifier: Modifier = Modifier,
    expanded: MutableState<Boolean> = rememberJetPrefDropdownExpandedState(),
    enabled: Boolean = true,
    isError: Boolean = false,
    labelText: String? = null,
    label: @Composable (() -> Unit)? = null,
    optionsLabelProvider: ((T) -> String)? = null,
    appearance: JetPrefTextFieldAppearance = JetPrefDropdownMenuDefaults.filled(),
) {
    fun asString(v: T): String {
        return optionsLabelProvider?.invoke(v) ?: v.toString()
    }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded.value,
        onExpandedChange = {
            if (enabled) {
                expanded.value = it
            }
        },
    ) {
        JetPrefTextField(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            value = asString(options[selectedOptionIndex]),
            onValueChange = {},
            enabled = enabled,
            readOnly = true,
            isError = isError,
            singleLine = true,
            labelText = labelText,
            label = label,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
            appearance = appearance,
        )
        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(asString(option), style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onSelectOption(index)
                        expanded.value = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

/**
 * Contains the default values used by [JetPrefDropdown].
 *
 * @since 0.2.0
 */
object JetPrefDropdownMenuDefaults {
    /**
     * Creates a filled [JetPrefTextFieldAppearance] with provided shape and color
     * for usage in [JetPrefDropdown].
     *
     * @since 0.2.0
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun filled(
        shape: Shape = TextFieldDefaults.shape,
        colors: TextFieldColors = ExposedDropdownMenuDefaults.textFieldColors(),
    ) = JetPrefTextFieldDefaults.filled(shape, colors)

    /**
     * Creates an outlined [JetPrefTextFieldAppearance] with provided shape and color
     * for usage in [JetPrefDropdown].
     *
     * @since 0.2.0
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun outlined(
        shape: Shape = TextFieldDefaults.shape,
        colors: TextFieldColors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
    ) = JetPrefTextFieldDefaults.outlined(shape, colors)
}

/**
 * Remembers a [MutableState] for the expanded state of a [JetPrefDropdown].
 *
 * @since 0.2.0
 */
@Composable
fun rememberJetPrefDropdownExpandedState(
    initialValue: Boolean = false,
): MutableState<Boolean> {
    return remember { mutableStateOf(initialValue) }
}
