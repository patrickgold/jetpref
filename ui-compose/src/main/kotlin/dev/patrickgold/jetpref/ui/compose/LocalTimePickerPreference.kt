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

import android.os.Build
import android.widget.TimePicker
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluatorScope
import dev.patrickgold.jetpref.datastore.model.PreferenceModel
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.ui.compose.annotations.ExperimentalJetPrefUi
import java.time.LocalTime

@ExperimentalJetPrefUi
data class ClockFormat(
    val is24Hour: Boolean,
    val showHours: Boolean,
    val showMinutes: Boolean,
    val showSeconds: Boolean,
    val showMilliseconds: Boolean,
)

object TimePickerDefaults {
    /**
     * Creates a clock format config, which can be used to determine
     * what UI controls a time picker should show.
     */
    @ExperimentalJetPrefUi
    fun clockFormat(
        is24Hour: Boolean = true,
        showHours: Boolean = true,
        showMinutes: Boolean = true,
        showSeconds: Boolean = false,
        showMilliseconds: Boolean = false,
    ) = ClockFormat(is24Hour, showHours, showMinutes, showSeconds, showMilliseconds)
}

/**
 * Shows a time picker dialog for setting a local time.
 *
 * Colors are currently messed up and controls are limited. This implementation
 * is highly experimental and likely to change completely or be removed soon.
 *
 * [clockFormat] attributes are currently not respected (expect is24Hour)
 */
@ExperimentalJetPrefUi
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun <T : PreferenceModel> PreferenceUiScope<T>.LocalTimePickerPreference(
    pref: PreferenceData<LocalTime>,
    @DrawableRes iconId: Int? = null,
    iconSpaceReserved: Boolean = this.iconSpaceReserved,
    title: String,
    dialogStrings: DialogPreferenceStrings = dialogPrefStrings(),
    clockFormat: ClockFormat = TimePickerDefaults.clockFormat(),
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {
    val prefValue by pref.observeAsState()
    var tmpDialogTimeValue by remember { mutableStateOf<LocalTime?>(null) }

    val evalScope = PreferenceDataEvaluatorScope.instance()
    if (this.visibleIf(evalScope) && visibleIf(evalScope)) {
        val isEnabled = this.enabledIf(evalScope) && enabledIf(evalScope)
        JetPrefListItem(
            icon = maybeJetIcon(iconId, iconSpaceReserved),
            text = title,
            secondaryText = prefValue.toString(),
            modifier = Modifier
                .clickable(
                    enabled = isEnabled,
                    role = Role.Button,
                    onClick = {
                        tmpDialogTimeValue = prefValue
                    },
                ),
            enabled = isEnabled,
        )
        if (tmpDialogTimeValue != null) {
            JetPrefAlertDialog(
                title = title,
                confirmLabel = dialogStrings.confirmLabel,
                onConfirm = {
                    pref.set(tmpDialogTimeValue!!)
                    tmpDialogTimeValue = null
                },
                dismissLabel = dialogStrings.dismissLabel,
                onDismiss = { tmpDialogTimeValue = null },
                neutralLabel = dialogStrings.neutralLabel,
                onNeutral = {
                    pref.reset()
                    tmpDialogTimeValue = null
                },
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) {
                Column {
                    AndroidView(
                        factory = { context ->
                            TimePicker(context).also {
                                it.setIs24HourView(clockFormat.is24Hour)
                                it.hour = tmpDialogTimeValue!!.hour
                                it.minute = tmpDialogTimeValue!!.minute
                                it.setOnTimeChangedListener { _, hours, minutes ->
                                    tmpDialogTimeValue = LocalTime.of(hours, minutes)
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
