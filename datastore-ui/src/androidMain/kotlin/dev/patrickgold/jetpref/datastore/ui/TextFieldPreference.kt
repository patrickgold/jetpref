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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import dev.patrickgold.jetpref.datastore.component.PreferenceComponent
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.collectAsState
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import dev.patrickgold.jetpref.material.ui.JetPrefTextField
import dev.patrickgold.jetpref.material.ui.JetPrefTextFieldDefaults
import dev.patrickgold.jetpref.material.ui.whenNotNullOrBlank
import kotlinx.coroutines.launch

/**
 * Material text field preference which provides a dialog with a text field.
 *
 * @param pref The string preference data entry from the datastore.
 * @param modifier Modifier to be applied to the underlying list item.
 * @param icon The [ImageVector] of the list entry.
 * @param title The title of this preference, shown as the list item primary text (max 1 line).
 * @param summaryIfBlank The summary of this preference if the state is blank.
 * @param summaryIfEmpty The summary of this preference if the state is empty.
 * @param summary The summary function of this preference, shown as the list item secondary text (max 2 lines). If
 *  this is specified it will override provided [summaryIfBlank] and [summaryIfEmpty].
 * @param transformValue The transformation function to transform the entered value before saving/validating it.
 * @param validateValue The validation function to check if the entered value is valid (after transformation). To
 *  indicate an invalid value, throw an exception.
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference should be enabled (true) or
 *  disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference should be visible (true) or
 *  hidden (false).
 *
 * @since 0.4.0
 */
@Composable
fun TextFieldPreference(
    pref: PreferenceData<String>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String,
    summaryIfBlank: String? = null,
    summaryIfEmpty: String? = null,
    summary: @Composable (String) -> String? = {
        when {
            it.isEmpty() -> summaryIfEmpty ?: it
            it.isBlank() -> summaryIfBlank ?: it
            else -> it
        }
    },
    transformValue: (String) -> String = { it },
    validateValue: (String) -> Unit = { },
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    val dialogStrings = LocalDialogPrefStrings.current
    val scope = rememberCoroutineScope()
    val prefValue by pref.collectAsState()
    var localPrefValue by remember { mutableStateOf("") }
    var isDialogOpen by remember { mutableStateOf(false) }

    Preference(
        modifier = modifier,
        icon = icon,
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
                scope.launch {
                    pref.set(transformValue(localPrefValue))
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
                    pref.reset()
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

@Deprecated("Use new TextFieldPreference instead.")
@Composable
fun TextFieldPreference(
    pref: PreferenceData<String>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = LocalIconSpaceReserved.current,
    title: String,
    summaryIfBlank: String? = null,
    summaryIfEmpty: String? = null,
    summary: @Composable (String) -> String? = {
        when {
            it.isEmpty() -> summaryIfEmpty ?: it
            it.isBlank() -> summaryIfBlank ?: it
            else -> it
        }
    },
    dialogStrings: DialogPrefStrings = LocalDialogPrefStrings.current,
    transformValue: (String) -> String = { it },
    validateValue: (String) -> Unit = { },
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    CompositionLocalProvider(
        LocalIconSpaceReserved provides iconSpaceReserved,
        LocalDialogPrefStrings provides dialogStrings,
    ) {
        TextFieldPreference(
            pref, modifier, icon, title, summaryIfBlank, summaryIfEmpty, summary, transformValue, validateValue,
            enabledIf,
        )
    }
}

@Composable
fun TextFieldPreference(
    component: PreferenceComponent.TextField,
    modifier: Modifier = Modifier,
) {
    TextFieldPreference(
        pref = component.pref,
        modifier = modifier,
        icon = component.icon?.invoke(),
        title = component.title.invoke(),
        summaryIfBlank = component.summaryIfBlank?.invoke(),
        summaryIfEmpty = component.summaryIfEmpty?.invoke(),
        summary = component.summary,
        transformValue = component.transformValue,
        validateValue = component.validateValue,
        enabledIf = component.enabledIf,
        visibleIf = component.visibleIf,
    )
}
