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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow

/**
 * A text field wrapper allowing to dynamically switch between filled and outlined.
 * See the original documentation for [TextField] and [OutlinedTextField] for more information.
 *
 * @since 0.2.0
 *
 * @see androidx.compose.material3.TextField
 * @see androidx.compose.material3.OutlinedTextField
 */
@Composable
fun JetPrefTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    labelText: String? = null,
    label: @Composable (() -> Unit)? = null,
    placeholderText: String? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    appearance: JetPrefTextFieldAppearance = JetPrefTextFieldDefaults.filled(),
) {
    val labelComposable = label ?: labelText?.let { {
        Text(text = it, maxLines = 1, overflow = TextOverflow.Ellipsis)
    } }
    val placeholderComposable = placeholder ?: placeholderText?.let { {
        Text(text = it, maxLines = 1, overflow = TextOverflow.Ellipsis)
    } }

    when (appearance.style) {
        JetPrefTextFieldStyle.Filled -> {
            TextField(
                value,
                onValueChange,
                modifier,
                enabled,
                readOnly,
                textStyle,
                labelComposable,
                placeholderComposable,
                leadingIcon,
                trailingIcon,
                prefix,
                suffix,
                supportingText,
                isError,
                visualTransformation,
                keyboardOptions,
                keyboardActions,
                singleLine,
                maxLines,
                minLines,
                interactionSource,
                appearance.shape,
                appearance.colors,
            )
        }
        JetPrefTextFieldStyle.Outlined -> {
            OutlinedTextField(
                value,
                onValueChange,
                modifier,
                enabled,
                readOnly,
                textStyle,
                labelComposable,
                placeholderComposable,
                leadingIcon,
                trailingIcon,
                prefix,
                suffix,
                supportingText,
                isError,
                visualTransformation,
                keyboardOptions,
                keyboardActions,
                singleLine,
                maxLines,
                minLines,
                interactionSource,
                appearance.shape,
                appearance.colors,
            )
        }
    }
}

/**
 * A text field wrapper allowing to dynamically switch between filled and outlined.
 * See the original documentation for [TextField] and [OutlinedTextField] for more information.
 *
 * @since 0.2.0
 *
 * @see androidx.compose.material3.TextField
 * @see androidx.compose.material3.OutlinedTextField
 */
@Composable
fun JetPrefTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    labelText: String? = null,
    label: @Composable (() -> Unit)? = null,
    placeholderText: String? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    appearance: JetPrefTextFieldAppearance = JetPrefTextFieldDefaults.filled(),
) {
    val labelComposable = label ?: labelText?.let { {
        Text(text = it, maxLines = 1, overflow = TextOverflow.Ellipsis)
    } }
    val placeholderComposable = placeholder ?: placeholderText?.let { {
        Text(text = it, maxLines = 1, overflow = TextOverflow.Ellipsis)
    } }

    when (appearance.style) {
        JetPrefTextFieldStyle.Filled -> {
            TextField(
                value,
                onValueChange,
                modifier,
                enabled,
                readOnly,
                textStyle,
                labelComposable,
                placeholderComposable,
                leadingIcon,
                trailingIcon,
                prefix,
                suffix,
                supportingText,
                isError,
                visualTransformation,
                keyboardOptions,
                keyboardActions,
                singleLine,
                maxLines,
                minLines,
                interactionSource,
                appearance.shape,
                appearance.colors,
            )
        }
        JetPrefTextFieldStyle.Outlined -> {
            OutlinedTextField(
                value,
                onValueChange,
                modifier,
                enabled,
                readOnly,
                textStyle,
                labelComposable,
                placeholderComposable,
                leadingIcon,
                trailingIcon,
                prefix,
                suffix,
                supportingText,
                isError,
                visualTransformation,
                keyboardOptions,
                keyboardActions,
                singleLine,
                maxLines,
                minLines,
                interactionSource,
                appearance.shape,
                appearance.colors,
            )
        }
    }
}

/**
 * The style of a [JetPrefTextField].
 *
 * @since 0.2.0
 */
enum class JetPrefTextFieldStyle {
    Filled,
    Outlined,
}

/**
 * The appearance of a [JetPrefTextField].
 *
 * @since 0.2.0
 */
data class JetPrefTextFieldAppearance(
    val style: JetPrefTextFieldStyle,
    val shape: Shape,
    val colors: TextFieldColors,
)

/**
 * Contains the default values used by [JetPrefTextField].
 *
 * @since 0.2.0
 */
object JetPrefTextFieldDefaults {
    /**
     * Creates a filled [JetPrefTextFieldAppearance] with provided shape and color
     * for usage in [JetPrefTextField].
     *
     * @since 0.2.0
     */
    @Composable
    fun filled(
        shape: Shape = TextFieldDefaults.shape,
        colors: TextFieldColors = TextFieldDefaults.colors(),
    ) = JetPrefTextFieldAppearance(JetPrefTextFieldStyle.Filled, shape, colors)

    /**
     * Creates an outlined [JetPrefTextFieldAppearance] with provided shape and color
     * for usage in [JetPrefTextField].
     *
     * @since 0.2.0
     */
    @Composable
    fun outlined(
        shape: Shape = OutlinedTextFieldDefaults.shape,
        colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    ) = JetPrefTextFieldAppearance(JetPrefTextFieldStyle.Outlined, shape, colors)
}
