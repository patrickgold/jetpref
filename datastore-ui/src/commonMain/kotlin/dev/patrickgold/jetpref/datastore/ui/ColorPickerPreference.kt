/*
 * Copyright 2025-2026 Patrick Goldinger
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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import dev.patrickgold.jetpref.datastore.component.PreferenceComponent
import dev.patrickgold.jetpref.datastore.model.collectAsState
import dev.patrickgold.jetpref.material.ui.AlphaBar
import dev.patrickgold.jetpref.material.ui.ExperimentalJetPrefMaterial3Ui
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import dev.patrickgold.jetpref.material.ui.JetPrefColorPicker
import dev.patrickgold.jetpref.material.ui.checkeredBackground
import dev.patrickgold.jetpref.material.ui.rememberJetPrefColorPickerState
import kotlinx.coroutines.launch

/**
 * Material Color picker preference with a preset and a custom layout
 *
 * @param component Component describing what to display.
 * @param modifier Modifier to be applied to the underlying preference.
 * @param transformValue Optional callback for overriding colors with other colors.
 *
 * @since 0.4.0
 */
@OptIn(ExperimentalJetPrefMaterial3Ui::class)
@Composable
fun ColorPickerPreference(
    component: PreferenceComponent.ColorPicker,
    modifier: Modifier = Modifier,
    transformValue: (Color) -> Color = { it },
) {
    val dialogStrings = LocalDialogPrefStrings.current
    val scope = rememberCoroutineScope()
    var showPicker by remember { mutableStateOf(false) }
    var dialogValue by remember { mutableIntStateOf(0) }
    val prefValue by component.pref.collectAsState()
    val safeValue = prefValue.safeValue()

    Preference(
        modifier = modifier,
        icon = component.icon?.invoke(),
        title = component.title.invoke(),
        summary = component.summary?.invoke(),
        trailing = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .checkeredBackground(gridSize = 2.dp)
                    .background(Color(safeValue))
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        },
        enabledIf = component.enabledIf,
        visibleIf = component.visibleIf,
        onClick = {
            dialogValue = safeValue
            showPicker = true
        }
    )

    if (showPicker) {
        val defaultColor = Color(component.pref.default.safeValue())
        val color = Color(dialogValue)
        val presetColors = remember {
            component.defaultColors.apply {
                if (component.defaultValueLabel == null) {
                    plus(defaultColor)
                }
            }
        }
        var selectedPreset by remember {
            mutableIntStateOf(
                presetColors.indexOf(
                    color.copy(alpha = 1f)
                )
            )
        }
        var advanced by remember {
            mutableStateOf(
                !presetColors.contains(color.copy(alpha = 1f))
                    && color.copy(1f)
                    != defaultColor && component.enableAdvancedLayout
            )
        }
        val neutralLabel by remember(advanced, component.enableAdvancedLayout) {
            mutableStateOf(
                if (!component.enableAdvancedLayout) {
                    null
                } else {
                    if (advanced) "Presets" else "Custom"
                }
            )
        }
        val colorPickerState = rememberJetPrefColorPickerState(color)

        key(advanced) {
            JetPrefAlertDialog(
                title = component.title.invoke(),
                scrollModifier = Modifier,
                content = {
                    if (advanced) {
                        selectedPreset = -1
                        JetPrefColorPicker(
                            state = colorPickerState,
                            onColorChange = {
                                dialogValue = it.toArgb()
                            },
                            withAlpha = component.showAlphaSlider,
                            saturationValueAspectRatio = 1f,
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(48.dp),
                                userScrollEnabled = false,
                                content = {
                                    items(presetColors.size) { index ->
                                        ColorBox(
                                            color = presetColors[index].copy(alpha = color.alpha),
                                            selected = selectedPreset == index,
                                            icon = component.icon?.invoke(),
                                            onSelect = {
                                                selectedPreset = index
                                                dialogValue = it.toArgb()
                                                colorPickerState.setColor(it)
                                            }
                                        )
                                    }
                                }
                            )

                            component.defaultValueLabel?.invoke()?.let { defaultValueLabel ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            selectedPreset = -1
                                            dialogValue = defaultColor.toArgb()
                                            colorPickerState.setColor(defaultColor)
                                        }
                                ) {
                                    ColorBox(
                                        color = defaultColor,
                                        selected = color.copy(1f) == defaultColor,
                                        icon = component.icon?.invoke(),
                                        onSelect = {
                                            selectedPreset = -1
                                            dialogValue = defaultColor.toArgb()
                                            colorPickerState.setColor(defaultColor)
                                        }
                                    )

                                    Text(
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        text = defaultValueLabel,
                                    )

                                }
                            }

                            if (component.showAlphaSlider) {
                                AlphaBar(
                                    onColorChange = {
                                        dialogValue = it.toArgb()
                                    },
                                    state = colorPickerState,
                                    strokeColor = Color.White
                                )
                            }
                        }
                    }
                },
                neutralLabel = neutralLabel,
                onNeutral = {
                    advanced = !advanced
                },
                neutralEnabled = component.enableAdvancedLayout,
                confirmLabel = dialogStrings.confirmLabel,
                onConfirm = {
                    scope.launch {
                        component.pref.set(transformValue(Color(dialogValue)))
                    }
                    showPicker = false
                },
                dismissLabel = dialogStrings.dismissLabel,
                onDismiss = { showPicker = false },
            )
        }
    }
}

/**
 * ColorBox used in the [ColorPickerPreference] for displaying the default colors.
 *
 * @param color The [Color] of the ColorBox
 * @param selected If the color box is currently selected.
 * @param icon the [ImageVector] that should be used as selected indicator.
 * @param onSelect callback for selecting the color of the color box.
 * @since 0.2.0
 */
@Composable
fun ColorBox(
    color: Color,
    selected: Boolean,
    icon: ImageVector?,
    onSelect: (Color) -> Unit,
) {
    Box(
        modifier = Modifier
            .requiredSize(50.dp)
            .padding(4.dp)
            .clip(CircleShape)
            //.background(MaterialTheme.colorScheme.surface)
            .checkeredBackground()
            .background(color)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .padding(1.dp)
            .border(
                1.dp,
                color.copy(alpha = 1f),
                shape = CircleShape
            )
            .clickable {
                onSelect(color)
            },
        contentAlignment = Alignment.Center
    ) {
        if (selected)

            Icon(
                imageVector = icon ?: return,
                contentDescription = null,
                tint = if (ColorUtils.calculateLuminance(color.toArgb()) < 0.5)
                    Color.White
                else
                    Color.Black
            )

    }
}

/**
 * Safe value
 *
 * @return the save color value. Either itself or the material you system accent
 * @since 0.2.0
 */
@Composable
expect fun Color.safeValue(): Int
