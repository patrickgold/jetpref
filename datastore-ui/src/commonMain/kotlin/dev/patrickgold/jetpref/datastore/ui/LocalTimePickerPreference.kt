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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.patrickgold.jetpref.datastore.component.PreferenceComponent
import dev.patrickgold.jetpref.datastore.model.LocalTime
import dev.patrickgold.jetpref.datastore.model.collectAsState
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import jetpref.datastore_ui.generated.resources.Res
import jetpref.datastore_ui.generated.resources.ic_keyboard
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
expect fun rememberIs24HourFormat(): State<Boolean>

/**
 * Material preference which provides a dialog with a time picker for choosing a time.
 *
 * @param component Component describing what to display.
 * @param modifier Modifier to be applied to the underlying preference.
 *
 * @since 0.4.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalTimePickerPreference(
    component: PreferenceComponent.LocalTimePicker,
    modifier: Modifier = Modifier,
) {
    val dialogStrings = LocalDialogPrefStrings.current
    val scope = rememberCoroutineScope()
    val prefValue by component.pref.collectAsState()
    var isDialogOpen by remember { mutableStateOf(false) }
    var displayMode by remember { mutableStateOf(DisplayMode.Picker) }
    val is24hour by rememberIs24HourFormat()

    Preference(
        modifier = modifier,
        icon = component.icon?.invoke(),
        title = component.title.invoke(),
        summary = prefValue.stringRepresentation(is24hour),
        enabledIf = component.enabledIf,
        visibleIf = component.visibleIf,
        onClick = {
            isDialogOpen = true
        },
    )

    if (isDialogOpen) {
        val timePickerState = rememberTimePickerState(
            initialHour = prefValue.hour,
            initialMinute = prefValue.minute,
            is24Hour = is24hour,
        )

        LaunchedEffect(is24hour) {
            timePickerState.is24hour = is24hour
        }

        JetPrefAlertDialog(
            title = component.title.invoke(),
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min),
            confirmLabel = dialogStrings.confirmLabel,
            onConfirm = {
                scope.launch {
                    component.pref.set(timePickerState.toLocalTime())
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
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Column {
                val contentModifier = Modifier.padding(horizontal = 24.dp)
                when (displayMode) {
                    DisplayMode.Picker -> TimePicker(modifier = contentModifier, state = timePickerState)
                    DisplayMode.Input -> TimeInput(modifier = contentModifier, state = timePickerState)
                }
                DisplayModeToggleButton(displayMode, onDisplayModeChange = { displayMode = it })
            }
        }
    }
}

@Composable
private fun DisplayModeToggleButton(
    displayMode: DisplayMode,
    onDisplayModeChange: (DisplayMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (displayMode) {
        DisplayMode.Picker -> IconButton(
            modifier = modifier,
            onClick = { onDisplayModeChange(DisplayMode.Input) },
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_keyboard),
                contentDescription = "Switch time input to keyboard input",
            )
        }

        DisplayMode.Input -> IconButton(
            modifier = modifier,
            onClick = { onDisplayModeChange(DisplayMode.Picker) },
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_keyboard),
                contentDescription = "Switch time input to time picker",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun TimePickerState.toLocalTime(): LocalTime {
    return LocalTime(hour = hour, minute = minute)
}
