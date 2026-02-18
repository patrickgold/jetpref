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

import android.text.format.DateFormat.is24HourFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.LifecycleResumeEffect
import dev.patrickgold.jetpref.datastore.component.PreferenceComponentItem
import dev.patrickgold.jetpref.datastore.model.LocalTime
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.collectAsState
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import kotlinx.coroutines.launch

/**
 * Material preference which provides a dialog with a time picker for choosing a time.
 *
 * @param pref The localTime preference data entry from the datastore.
 * @param modifier Modifier to be applied to the underlying list item.
 * @param icon The [ImageVector] of the list entry.
 * @param title The title of this preference, shown as the list item primary text (max 1 line).
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference should be
 *  enabled (true) or disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference should be
 *  visible (true) or hidden (false).
 *
 * @since 0.4.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalTimePickerPreference(
    pref: PreferenceData<LocalTime>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    val context = LocalContext.current
    val dialogStrings = LocalDialogPrefStrings.current
    val scope = rememberCoroutineScope()
    val prefValue by pref.collectAsState()
    var isDialogOpen by remember { mutableStateOf(false) }
    var displayMode by remember { mutableStateOf(DisplayMode.Picker) }
    var is24hour by remember {
        mutableStateOf(is24HourFormat(context))
    }

    LifecycleResumeEffect(Unit) {
        is24hour = is24HourFormat(context)
        onPauseOrDispose { }
    }

    Preference(
        modifier = modifier,
        icon = icon,
        title = title,
        summary = prefValue.stringRepresentation(is24hour),
        enabledIf = enabledIf,
        visibleIf = visibleIf,
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

        LifecycleResumeEffect(Unit) {
            timePickerState.is24hour = is24HourFormat(context)
            onPauseOrDispose { }
        }

        JetPrefAlertDialog(
            title = title,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min),
            confirmLabel = dialogStrings.confirmLabel,
            onConfirm = {
                scope.launch {
                    pref.set(timePickerState.toLocalTime())
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

@Deprecated("Use new LocalTimePickerPreference instead.")
@Composable
fun LocalTimePickerPreference(
    pref: PreferenceData<LocalTime>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = LocalIconSpaceReserved.current,
    title: String,
    dialogStrings: DialogPrefStrings = LocalDialogPrefStrings.current,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    CompositionLocalProvider(
        LocalIconSpaceReserved provides iconSpaceReserved,
        LocalDialogPrefStrings provides dialogStrings,
    ) {
        LocalTimePickerPreference(pref, modifier, icon, title, enabledIf, visibleIf)
    }
}

@Composable
fun LocalTimePickerPreference(
    component: PreferenceComponentItem.LocalTimePicker,
    modifier: Modifier = Modifier,
) {
    LocalTimePickerPreference(
        pref = component.pref,
        modifier = modifier,
        icon = component.icon?.invoke(),
        title = component.title.invoke(),
        enabledIf = component.enabledIf,
        visibleIf = component.visibleIf,
    )
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
