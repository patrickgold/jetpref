/*
 * Copyright 2024-2026 Patrick Goldinger
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

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.patrickgold.jetpref.datastore.component.PreferenceComponent
import dev.patrickgold.jetpref.datastore.model.collectAsState
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import dev.patrickgold.jetpref.material.ui.JetPrefTextField
import dev.patrickgold.jetpref.material.ui.JetPrefTextFieldDefaults
import dev.patrickgold.jetpref.material.ui.whenNotNullOrBlank
import kotlinx.coroutines.launch

/**
 * Material text field preference which provides a dialog with a text field.
 *
 * @param component Component describing what to display.
 * @param modifier Modifier to be applied to the underlying preference.
 *
 * @since 0.4.0
 */
@Composable
fun TextFieldPreference(
    component: PreferenceComponent.TextField,
    modifier: Modifier = Modifier,
) {
    val dialogStrings = LocalDialogPrefStrings.current
    val scope = rememberCoroutineScope()
    val prefValue by component.pref.collectAsState()
    var localPrefValue by remember { mutableStateOf("") }
    var isDialogOpen by remember { mutableStateOf(false) }

    Preference(
        modifier = modifier,
        icon = component.icon?.invoke(),
        title = component.title.invoke(),
        summary = component.summary.invoke(prefValue),
        enabledIf = component.enabledIf,
        visibleIf = component.visibleIf,
        onClick = {
            localPrefValue = component.pref.get()
            isDialogOpen = true
        },
    )

    if (isDialogOpen) {
        val validationResult = remember(localPrefValue) {
            runCatching {
                component.validateValue(component.transformValue(localPrefValue))
            }
        }
        JetPrefAlertDialog(
            title = component.title.invoke(),
            confirmLabel = dialogStrings.confirmLabel,
            confirmEnabled = validationResult.isSuccess,
            onConfirm = {
                scope.launch {
                    component.pref.set(component.transformValue(localPrefValue))
                }
                isDialogOpen = false
            },
            dismissLabel = dialogStrings.dismissLabel,
            onDismiss = {
                isDialogOpen = false
            },
            neutralLabel = dialogStrings.neutralLabel,
            onNeutral = {
                scope.launch {
                    component.pref.reset()
                }
                isDialogOpen = false
            },
        ) {
            Column {
                val message = remember(validationResult) {
                    validationResult.exceptionOrNull()?.let { error ->
                        error.localizedMessage ?: error.message
                    }
                }
                JetPrefTextField(
                    value = localPrefValue,
                    onValueChange = { localPrefValue = it },
                    isError = validationResult.isFailure,
                    supportingText = whenNotNullOrBlank(message) { Text(it) },
                    appearance = JetPrefTextFieldDefaults.outlined(),
                )
            }
        }
    }
}
