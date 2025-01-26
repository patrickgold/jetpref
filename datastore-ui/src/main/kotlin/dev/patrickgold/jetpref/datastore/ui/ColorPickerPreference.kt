package dev.patrickgold.jetpref.datastore.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import dev.patrickgold.jetpref.datastore.model.PreferenceData
import dev.patrickgold.jetpref.datastore.model.PreferenceDataEvaluator
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.material.ui.AlphaBar
import dev.patrickgold.jetpref.material.ui.ExperimentalJetPrefMaterial3Ui
import dev.patrickgold.jetpref.material.ui.JetPrefColorPicker
import dev.patrickgold.jetpref.material.ui.checkeredBackground
import dev.patrickgold.jetpref.material.ui.rememberJetPrefColorPickerState


@OptIn(ExperimentalMaterial3Api::class, ExperimentalJetPrefMaterial3Ui::class)
@Composable
fun ColorPickerPreference(
    pref: PreferenceData<Color>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconSpaceReserved: Boolean = LocalIconSpaceReserved.current,
    title: String,
    summary: String? = null,
    defaultValueLabel: String? = null,
    showAlphaSlider: Boolean = false,
    enableAdvancedLayout: Boolean = false,
    defaultColors: Array<Color>,
    dialogStrings: DialogPrefStrings = LocalDefaultDialogPrefStrings.current,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {

    var showPicker by remember { mutableStateOf(false) }
    var dialogValue by remember { mutableIntStateOf(0) }
    val prefValue by pref.observeAsState()
    val safeValue = prefValue.safeValue()
    val isMaterialYou = Color(dialogValue).isMaterialYou()


    Preference(
        modifier = modifier,
        icon = icon,
        iconSpaceReserved = iconSpaceReserved,
        trailing = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .background(Color(safeValue))
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        },
        title = title,
        summary = summary,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
        onClick = {
            dialogValue = safeValue
            showPicker = true
        }
    )

    if (showPicker) {
        val defaultColor = Color(pref.default.safeValue())

        val color = Color(dialogValue)
        val presetColors = remember {
            defaultColors.apply {
                if (defaultValueLabel == null) {
                    plus(defaultColor)
                }
            }
        }

        var selectedPreset by remember { mutableIntStateOf(presetColors.indexOf(color)) }
        var advanced by remember { mutableStateOf(false) }

        val colorPickerState = rememberJetPrefColorPickerState(color)

        key(advanced) {
            AlertDialog(
                onDismissRequest = { showPicker = false },
                title = { Text(title) },
                text = {
                    if (advanced) {
                        selectedPreset = -1
                        JetPrefColorPicker(
                            state = colorPickerState,
                            onColorChange = {
                                dialogValue = it.toArgb()
                            },
                            withAlpha = showAlphaSlider,
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
                                            icon = icon,
                                            onSelect = {
                                                selectedPreset = index
                                                dialogValue = it.toArgb()
                                                colorPickerState.setColor(it)
                                            }
                                        )
                                    }
                                }
                            )

                            defaultValueLabel?.let {
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
                                        selected = color == defaultColor,
                                        icon = icon,
                                        onSelect = {
                                            selectedPreset = -1
                                            dialogValue = defaultColor.toArgb()
                                            colorPickerState.setColor(defaultColor)
                                        }
                                    )

                                    Text(
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        text = defaultValueLabel
                                    )

                                }
                            }

                            if (showAlphaSlider) {
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
                confirmButton = {
                    // Workaround for adding a third button
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (enableAdvancedLayout) {
                            TextButton(
                                onClick = {
                                    advanced = !advanced
                                }) {
                                Text(
                                    if (advanced) "Presets" else "Custom"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        TextButton(onClick = { showPicker = false }) {
                            Text(dialogStrings.dismissLabel)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(
                            onClick = {
                                showPicker = false
                                if (isMaterialYou) {
                                    pref.set(Color.Unspecified)
                                } else {
                                    pref.set(Color(dialogValue))
                                }
                            }) {
                            Text(dialogStrings.confirmLabel)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ColorBox(
    color: Color,
    selected: Boolean,
    icon: ImageVector?,
    onSelect: (Color) -> Unit,
) {
    Box(
        modifier = Modifier
            .requiredSize(56.dp)
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

@Composable
fun Color.safeValue(): Int {
    return if (isUnspecified && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        with(LocalContext.current) {
            resources.getColor(android.R.color.system_accent1_500, theme)
        }
    } else {
        toArgb()
    }
}

@Composable
fun Color.isMaterialYou(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        with(LocalContext.current) {
            this@isMaterialYou == Color(resources.getColor(android.R.color.system_accent1_500, theme))
        }
    } else {
        false
    }
}
