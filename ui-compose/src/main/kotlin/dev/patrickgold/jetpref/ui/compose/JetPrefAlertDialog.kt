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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (allowOutsideDismissal) onDismiss() },
        title = { Text(title) },
        text = content,
        buttons = {
            Row(modifier = Modifier.padding(top = 0.dp, bottom = 12.dp, start = 8.dp, end = 8.dp)) {
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
    )
}
