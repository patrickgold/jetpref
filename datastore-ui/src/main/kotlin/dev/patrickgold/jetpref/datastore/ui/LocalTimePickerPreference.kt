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

package dev.patrickgold.jetpref.datastore.ui

import android.text.format.DateFormat.is24HourFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
//TODO: Decide if the icons should be included here or in another module
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.patrickgold.jetpref.datastore.model.LocalTime
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalTimePickerPreference(
    pref: PreferenceData<LocalTime>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = LocalIconSpaceReserved.current,
    title: String,
    dialogStrings: DialogPrefStrings = LocalDefaultDialogPrefStrings.current,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    val prefValue by pref.observeAsState()
    var isDialogOpen by remember { mutableStateOf(false) }
    var displayMode by remember { mutableStateOf(DisplayMode.Picker) }

    Preference(
        modifier = modifier,
        icon = icon,
        iconSpaceReserved = iconSpaceReserved,
        title = title,
        summary = prefValue.stringRepresentation,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        onClick = {
            isDialogOpen = true
        }
    )

    if (isDialogOpen) {
        val is24Hour = is24HourFormat(LocalContext.current)
        val timePickerState = rememberTimePickerState(
            initialHour = prefValue.hour,
            initialMinute = prefValue.minute,
            is24Hour = is24Hour
        )

        JetPrefAlertDialog(
            title = title,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min),
            confirmLabel = dialogStrings.confirmLabel,
            onConfirm = {
                pref.set(timePickerState.toLocalTime())
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
            properties = DialogProperties(usePlatformDefaultWidth = false)
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

@OptIn(ExperimentalMaterial3Api::class)
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
                imageVector = Icons.Default.Keyboard,
                contentDescription = "Switch time input to keyboard input",
            )
        }

        DisplayMode.Input -> IconButton(
            modifier = modifier,
            onClick = { onDisplayModeChange(DisplayMode.Picker) },
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Switch time input to time picker",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun TimePickerState.toLocalTime(): LocalTime {
    return LocalTime(hour = hour, minute = minute)
}
