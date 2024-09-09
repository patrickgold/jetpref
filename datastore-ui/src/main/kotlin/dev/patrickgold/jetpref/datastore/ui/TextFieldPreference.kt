/*
 * Copyright 2024 Patrick Goldinger
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import dev.patrickgold.jetpref.material.ui.whenNotNullOrBlank

/**
 * Material text field preference which provides a dialog with a text field.
 *
 * @param pref The string preference data entry from the datastore.
 * @param modifier Modifier to be applied to the underlying list item.
 * @param icon The [ImageVector] of the list entry.
 * @param iconSpaceReserved Whether the icon space should be reserved even if no icon is provided.
 * @param title The title of this preference, shown as the list item primary text (max 1 line).
 * @param summaryIfBlank The summary of this preference if the state is blank.
 * @param summaryIfEmpty The summary of this preference if the state is empty.
 * @param summary The summary function of this preference, shown as the list item secondary text (max 2 lines). If
 *  this is specified it will override provided [summaryIfBlank] and [summaryIfEmpty].
 * @param dialogStrings The dialog strings to use for this dialog. Defaults to the current dialog prefs set.
 * @param transformValue The transformation function to transform the entered value before saving/validating it.
 * @param validateValue The validation function to check if the entered value is valid (after transformation). To
 *  indicate an invalid value, throw an exception.
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference should be enabled (true) or
 *  disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference should be visible (true) or
 *  hidden (false).
 *
 * @since 0.2.0
 */
@Composable
fun TextFieldPreference(
    pref: PreferenceData<String>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = LocalIconSpaceReserved.current,
    title: String,
    summaryIfBlank: String? = null,
    summaryIfEmpty: String? = null,
    summary: (String) -> String? = {
        when {
            it.isEmpty() -> summaryIfEmpty ?: it
            it.isBlank() -> summaryIfBlank ?: it
            else -> it
        }
    },
    dialogStrings: DialogPrefStrings = LocalDefaultDialogPrefStrings.current,
    transformValue: (String) -> String = { it },
    validateValue: (String) -> Unit = { },
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    val prefValue by pref.observeAsState()
    var localPrefValue by remember { mutableStateOf("") }
    var isDialogOpen by remember { mutableStateOf(false) }

    Preference(
        modifier = modifier,
        icon = icon,
        iconSpaceReserved = iconSpaceReserved,
        title = title,
        summary = summary(prefValue),
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        onClick = {
            localPrefValue = pref.get()
            isDialogOpen = true
        },
    )

    if (isDialogOpen) {
        val validationResult = remember(localPrefValue) {
            runCatching {
                validateValue(transformValue(localPrefValue))
            }
        }
        JetPrefAlertDialog(
            title = title,
            confirmLabel = dialogStrings.confirmLabel,
            confirmEnabled = validationResult.isSuccess,
            onConfirm = {
                pref.set(transformValue(localPrefValue))
                isDialogOpen = false
            },
            dismissLabel = dialogStrings.dismissLabel,
            onDismiss = {
                isDialogOpen = false
            },
            neutralLabel = dialogStrings.neutralLabel,
            onNeutral = {
                pref.reset()
                isDialogOpen = false
            },
        ) {
            Column {
                val message = remember(validationResult) {
                    validationResult.exceptionOrNull()?.let { error ->
                        error.localizedMessage ?: error.message
                    }
                }
                OutlinedTextField(
                    value = localPrefValue,
                    onValueChange = { localPrefValue = it },
                    isError = validationResult.isFailure,
                    supportingText = whenNotNullOrBlank(message) { Text(it) },
                )
            }
        }
    }
}
