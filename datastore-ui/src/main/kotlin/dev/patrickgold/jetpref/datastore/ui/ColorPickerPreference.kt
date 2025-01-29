package dev.patrickgold.jetpref.datastore.ui

import android.content.Context
import android.os.Build
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
import dev.patrickgold.jetpref.material.ui.JetPrefAlertDialog
import dev.patrickgold.jetpref.material.ui.JetPrefColorPicker
import dev.patrickgold.jetpref.material.ui.checkeredBackground
import dev.patrickgold.jetpref.material.ui.rememberJetPrefColorPickerState


/**
 * Material Color picker preference with a preset and a custom layout
 *
 * @param pref The color preference data entry from the datastore.
 * @param modifier Modifier to be applied to the underlying list item.
 * @param icon The [ImageVector] of the list entry.
 * @param iconSpaceReserved If the space at the start of the list item should be reserved (blank
 *  space) if no icon ID is provided.
 * @param title The title of this preference, shown as the list item primary text (max 1 line).
 * @param summary The summary of this preference, shown as the list item secondary text (max 2 lines).
 * @param defaultValueLabel The label for the default color box.
 * @param showAlphaSlider If the alpha slider should be shown.
 * @param enableAdvancedLayout If the advancedLayout should be shown.
 * @param defaultColors [Array] of [Color] which will be shown as default colors.
 * @param colorOverride Optional callback for overriding colors with other colors.
 * @param dialogStrings The dialog strings to use for this dialog. Defaults to the current dialog prefs set.
 * @param enabledIf Evaluator scope which allows to dynamically decide if this preference should be
 *  enabled (true) or disabled (false).
 * @param visibleIf Evaluator scope which allows to dynamically decide if this preference should be
 *  visible (true) or hidden (false).
 * @since 0.2.0
 */
@OptIn(ExperimentalJetPrefMaterial3Ui::class)
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
    colorOverride: (Color) -> Color = { it },
    dialogStrings: DialogPrefStrings = LocalDefaultDialogPrefStrings.current,
    enabledIf: PreferenceDataEvaluator = { true },
    visibleIf: PreferenceDataEvaluator = { true },
) {

    var showPicker by remember { mutableStateOf(false) }
    var dialogValue by remember { mutableIntStateOf(0) }
    val prefValue by pref.observeAsState()
    val safeValue = prefValue.safeValue()


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
                    .checkeredBackground(gridSize = 2.dp)
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

        val neutralLabel by remember(advanced, enableAdvancedLayout) {
            mutableStateOf(
                if (!enableAdvancedLayout) {
                    null
                } else {
                    if (advanced) "Presets" else "Custom"
                }
            )
        }


        val colorPickerState = rememberJetPrefColorPickerState(color)

        key(advanced) {
            JetPrefAlertDialog(
                title = title,
                scrollModifier = Modifier,
                content = {
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
                neutralLabel = neutralLabel,
                onNeutral = {
                    advanced = !advanced
                },
                neutralEnabled = enableAdvancedLayout,
                confirmLabel = dialogStrings.confirmLabel,
                onConfirm = {
                    showPicker = false
                    pref.set(colorOverride(Color(dialogValue)))
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
 * @param selected If the colorbox is currently selected.
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

/**
 * Safe value
 *
 * @return the save color value. Either itself or the material you system accent
 * @since 0.2.0
 */
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


/**
 * Check if a color is the system accent color
 *
 * @param context
 * @return a boolean indicating if it is the material you color
 * @since 0.2.0
 */
fun Color.isMaterialYou(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        with(context) {
            this@isMaterialYou == Color(resources.getColor(android.R.color.system_accent1_500, theme))
        }
    } else {
        false
    }
}
