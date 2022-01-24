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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Material Design alert dialog allowing for full customization of the dialog appearance,
 * content and behavior.
 *
 * @param title The title of the Dialog which should specify the purpose of the Dialog.
 * @param modifier Modifier to be applied to the layout of the dialog.
 * @param confirmLabel The label of the confirm button of this dialog. Used to control the
 *  visibility of the confirm button. Passing null or a blank string will hide the button,
 *  any other string will show it.
 * @param confirmColors The colors to apply to the confirm button, if it is visible.
 * @param onConfirm Action to execute when the confirm button is pressed.
 * @param dismissLabel The label of the dismiss button of this dialog. Used to control the
 *  visibility of the dismiss button. Passing null or a blank string will hide the button,
 *  any other string will show it.
 * @param dismissColors The colors to apply to the dismiss button, if it is visible.
 * @param onDismiss Action to execute when the dismiss button is pressed.
 * @param neutralLabel The label of the neutral button of this dialog. Used to control the
 *  visibility of the neutral button. Passing null or a blank string will hide the button,
 *  any other string will show it.
 * @param neutralColors The colors to apply to the neutral button, if it is visible.
 * @param onNeutral Action to execute when the neutral button is pressed.
 * @param allowOutsideDismissal Specify if a user can dismiss the Dialog by clicking outside
 *  or pressing the back button.
 * @param onOutsideDismissal Action to execute when [allowOutsideDismissal] is true and an
 *  outside dismissal occurs. This is not called when the dismiss button is pressed. Defaults
 *  to the same action as [onDismiss].
 * @param trailingIconTitle Specify an icon / UI control to be placed in trailing position to
 *  the dialog title.
 * @param properties Dialog properties for further customization of this dialog's behavior.
 * @param scrollModifier The scroll modifier to apply to the inner content box. Defaults to
 *  a simple vertical scroll modifier. Pass an empty modifier to disable scrolling entirely.
 * @param shape The shape of this dialog.
 * @param backgroundColor The background color of this dialog.
 * @param contentColor The content color of this dialog.
 * @param contentPadding Specify a padding to apply to the inner content box.
 * @param content The content to be displayed inside the dialog.
 *
 * @since 0.1.0
 *
 * @see androidx.compose.material.AlertDialog
 * @see androidx.compose.ui.window.Dialog
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun JetPrefAlertDialog(
    title: String,
    modifier: Modifier = Modifier,
    confirmLabel: String? = null,
    confirmColors: ButtonColors = ButtonDefaults.textButtonColors(),
    onConfirm: () -> Unit = { },
    dismissLabel: String? = null,
    dismissColors: ButtonColors = ButtonDefaults.textButtonColors(),
    onDismiss: () -> Unit = { },
    neutralLabel: String? = null,
    neutralColors: ButtonColors = ButtonDefaults.textButtonColors(),
    onNeutral: () -> Unit = { },
    allowOutsideDismissal: Boolean = true,
    onOutsideDismissal: () -> Unit = onDismiss,
    trailingIconTitle: @Composable () -> Unit = { },
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    scrollModifier: Modifier = Modifier.verticalScroll(rememberScrollState()),
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    contentPadding: PaddingValues = JetPrefAlertDialogDefaults.ContentPadding,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = { if (allowOutsideDismissal) onOutsideDismissal() },
        properties = properties,
    ) {
        Surface(
            modifier = modifier
                .padding(vertical = 16.dp, horizontal = 16.dp)
                .widthIn(max = JetPrefAlertDialogDefaults.MaxDialogWidth),
            shape = shape,
            color = backgroundColor,
            contentColor = contentColor,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .height(64.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.weight(1.0f),
                        text = title,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    trailingIconTitle()
                }
                Box(
                    modifier = Modifier
                        .padding(contentPadding)
                        .weight(1.0f, fill = false)
                        .fillMaxWidth()
                        .then(scrollModifier),
                ) {
                    content()
                }
                Row(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                    if (neutralLabel != null && neutralLabel.isNotBlank()) {
                        TextButton(
                            onClick = onNeutral,
                            modifier = Modifier.padding(end = 8.dp),
                            colors = neutralColors,
                        ) {
                            Text(neutralLabel)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1.0f))
                    if (dismissLabel != null && dismissLabel.isNotBlank()) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.padding(end = 8.dp),
                            colors = dismissColors,
                        ) {
                            Text(dismissLabel)
                        }
                    }
                    if (confirmLabel != null && confirmLabel.isNotBlank()) {
                        TextButton(
                            onClick = onConfirm,
                            colors = confirmColors,
                        ) {
                            Text(confirmLabel)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Contains the default values and other useful constants used by [JetPrefAlertDialog].
 */
object JetPrefAlertDialogDefaults {
    /**
     * The default content padding for [JetPrefAlertDialog].
     */
    val ContentPadding = PaddingValues(all = 24.dp)

    /**
     * The maximum dialog width for [JetPrefAlertDialog].
     */
    val MaxDialogWidth = 320.dp
}
