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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun JetPrefAlertDialog(
    title: String,
    confirmLabel: String? = null,
    onConfirm: () -> Unit = { },
    neutralLabel: String? = null,
    onNeutral: () -> Unit = { },
    dismissLabel: String? = null,
    onDismiss: () -> Unit = { },
    allowOutsideDismissal: Boolean = true,
    trailingIconTitle: @Composable () -> Unit = { },
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp),
    properties: DialogProperties = DialogProperties(),
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    content: @Composable () -> Unit,
) {
    val scrollState = rememberScrollState()
    Dialog(
        onDismissRequest = { if (allowOutsideDismissal) onDismiss() },
        properties = properties,
    ) {
        Surface(
            modifier = Modifier.padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.medium,
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
                        .verticalScroll(scrollState),
                ) {
                    content()
                }
                Row(modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)) {
                    val colors = ButtonDefaults.textButtonColors()
                    val elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp)
                    if (neutralLabel != null && neutralLabel.isNotBlank()) {
                        Button(
                            onClick = onNeutral,
                            colors = colors,
                            elevation = elevation,
                        ) {
                            Text(neutralLabel)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1.0f))
                    if (dismissLabel != null && dismissLabel.isNotBlank()) {
                        Button(
                            onClick = onDismiss,
                            colors = colors,
                            elevation = elevation,
                        ) {
                            Text(dismissLabel)
                        }
                    }
                    if (confirmLabel != null && confirmLabel.isNotBlank()) {
                        Button(
                            onClick = onConfirm,
                            colors = colors,
                            elevation = elevation,
                        ) {
                            Text(confirmLabel)
                        }
                    }
                }
            }
        }
    }
}
