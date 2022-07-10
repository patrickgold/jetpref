/*
 * Copyright 2022 Patrick Goldinger
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

package dev.patrickgold.jetpref.material.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

private val PreviewSize = 64.dp
private val SliderPadding = PaddingValues(all = 12.dp)
private val SliderTrackHeight = 12.dp
private val SliderThumbSize = 24.dp
private val SliderThumbBorderSize = 4.dp
private val SliderThumbElevation = 4.dp

private val ZeroOneRange = 0f..1f

/**
 * Material Design HSV color picker layout. Provides the ability to select the hue, saturation, value
 * and optionally also alpha of a color and report the current color back via [onColorChange].
 *
 * @param onColorChange The callback to invoke when a color change has occurred. The color delivered here
 *  is always the end product, taking all input fields in regard.
 * @param modifier THe modifier to apply to this layout.
 * @param state The state for this color picker.
 * @param alphaSlider If true, the layout shows a slider for modifying the alpha property of a color.
 * @param strokeColor The color of the thumb color.
 *
 * @since 0.1.0
 */
@ExperimentalJetPrefMaterialUi
@Composable
fun JetPrefColorPicker(
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
    state: JetPrefColorPickerState = rememberJetPrefColorPickerState(initColor = Color.White),
    alphaSlider: Boolean = true,
    strokeColor: Color = Color.White,
) {
    SideEffect {
        if (!alphaSlider) {
            state.alpha = 1f
        }
    }

    Column(modifier = modifier) {
        SaturationValueBox(onColorChange, state, strokeColor)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(PreviewSize)
                    .clip(CircleShape)
                    .checkeredBackground(),
                color = state.rememberColor(),
                content = { },
            )
            Column(modifier = Modifier.weight(1f)) {
                HueBar(onColorChange, state, strokeColor)
                if (alphaSlider) {
                    AlphaBar(onColorChange, state, strokeColor)
                }
            }
        }
    }
}

/**
 * Adaptive checkered background which draws behind the content. Used to display semi or fully
 * transparent surfaces to the user, when the transparency aspect of said surface must be clearly
 * communicated.
 *
 * When using the default colors, this modifier will work for both light and dark themes by using
 * (semi-)transparent grid colors.
 *
 * @param gridSize The grid size of the checkered background.
 * @param evenColor The even color of this checkered background (zero-based indexing).
 * @param oddColor The odd color of this checkered background (zero-based indexing).
 *
 * @since 0.1.0
 */
fun Modifier.checkeredBackground(
    gridSize: Dp = Dp.Unspecified,
    evenColor: Color = Color.Unspecified,
    oddColor: Color = Color.Unspecified,
): Modifier = composed {
    val even = evenColor.takeOrElse { MaterialTheme.colors.onBackground.copy(alpha = 0.160784314f) }
    val odd = oddColor.takeOrElse { Color.Transparent }

    this.drawBehind {
        val gridSizePx = gridSize.takeOrElse { SliderTrackHeight / 2f }.toPx()
        val cellCountX = ceil(this.size.width / gridSizePx).toInt()
        val cellCountY = ceil(this.size.height / gridSizePx).toInt()
        for (i in 0 until cellCountX) {
            for (j in 0 until cellCountY) {
                val color = if ((i + j) % 2 == 0) even else odd

                val x = i * gridSizePx
                val y = j * gridSizePx
                val size = Size(
                    width = if (i + 1 < cellCountX) gridSizePx else this.size.width % gridSizePx,
                    height = if (j + 1 < cellCountY) gridSizePx else this.size.height % gridSizePx,
                )
                drawRect(color, Offset(x, y), size)
            }
        }
    }
}

@Composable
private fun HueBar(
    onColorChange: (Color) -> Unit,
    state: JetPrefColorPickerState,
    strokeColor: Color,
) = with(LocalDensity.current) {
    BoxWithConstraints(
        modifier = Modifier
            .padding(SliderPadding)
            .fillMaxWidth()
            .height(SliderTrackHeight),
    ) {
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val hueBarShader = remember(width, height) {
            HueBarShader(width, height)
        }

        val inputModifier = Modifier.pointerInput(hueBarShader) {
            fun updateSlider(newPosition: Offset) {
                state.setHue(newPosition.x, width.toFloat())
                onColorChange(state.color())
            }

            forEachGesture {
                awaitPointerEventScope {
                    val down = awaitFirstDown()
                    updateSlider(down.position)
                    drag(down.id) { change ->
                        change.consumePositionChange()
                        updateSlider(change.position)
                    }
                }
            }
        }

        ColorSlider(
            modifier = inputModifier.padding(),
            bitmap = hueBarShader.image,
            fillColor = state.rememberHueColor(),
            strokeColor = strokeColor,
            thumbPosition = width.toDp() * (state.hue / 360f) - SliderThumbSize / 2,
        )
    }
}

@Composable
private fun AlphaBar(
    onColorChange: (Color) -> Unit,
    state: JetPrefColorPickerState,
    strokeColor: Color,
) = with(LocalDensity.current) {
    BoxWithConstraints(
        modifier = Modifier
            .padding(SliderPadding)
            .fillMaxWidth()
            .height(SliderTrackHeight),
    ) {
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val colorWithoutAlpha = state.rememberColorWithoutAlpha()
        val alphaBarShader = remember(width, height, colorWithoutAlpha) {
            AlphaBarShader(width, height, colorWithoutAlpha)
        }

        val inputModifier = Modifier.pointerInput(alphaBarShader) {
            fun updateSlider(newPosition: Offset) {
                state.setAlpha(newPosition.x, width.toFloat())
                onColorChange(state.color())
            }

            forEachGesture {
                awaitPointerEventScope {
                    val down = awaitFirstDown()
                    updateSlider(down.position)
                    drag(down.id) { change ->
                        change.consumePositionChange()
                        updateSlider(change.position)
                    }
                }
            }
        }

        ColorSlider(
            modifier = inputModifier,
            bitmap = alphaBarShader.image,
            fillColor = state.rememberColor(),
            strokeColor = strokeColor,
            thumbPosition = width.toDp() * state.alpha - (SliderThumbSize / 2),
            checkeredBackground = true,
        )
    }
}

@Composable
private fun SaturationValueBox(
    onColorChange: (Color) -> Unit,
    state: JetPrefColorPickerState,
    strokeColor: Color,
) = with(LocalDensity.current) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f),
    ) {
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val hueColor = state.rememberHueColor()
        val satValBox = remember(width, height, hueColor) {
            SaturationValueBoxShader(width, height, hueColor)
        }

        val inputModifier = Modifier.pointerInput(satValBox) {
            fun updateBox(newPosition: Offset) {
                state.setSaturation(newPosition.x, width.toFloat())
                state.setValue(newPosition.y, height.toFloat())
                onColorChange(state.color())
            }

            forEachGesture {
                awaitPointerEventScope {
                    val down = awaitFirstDown()
                    updateBox(down.position)
                    drag(down.id) { change ->
                        change.consumePositionChange()
                        updateBox(change.position)
                    }
                }
            }
        }

        Image(
            modifier = inputModifier.fillMaxSize(),
            contentDescription = null,
            bitmap = satValBox.image,
        )

        Thumb(
            fillColor = state.rememberColorWithoutAlpha(),
            strokeColor = strokeColor,
            thumbPositionX = width.toDp() * state.saturation - (SliderThumbSize / 2),
            thumbPositionY = height.toDp() * (1f - state.value) - (SliderThumbSize / 2),
        )
    }
}

@Composable
private fun ColorSlider(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap,
    fillColor: Color,
    strokeColor: Color,
    thumbPosition: Dp = 0.dp,
    checkeredBackground: Boolean = false,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .then(if (checkeredBackground) Modifier.checkeredBackground() else Modifier),
            contentDescription = null,
            bitmap = bitmap,
        )

        Thumb(
            fillColor = fillColor,
            strokeColor = strokeColor,
            thumbPositionX = thumbPosition,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Thumb(
    modifier: Modifier = Modifier,
    fillColor: Color,
    strokeColor: Color,
    thumbPositionX: Dp = 0.dp,
    thumbPositionY: Dp = 0.dp,
) {
    Surface(
        modifier = modifier
            .requiredSize(SliderThumbSize)
            .offset(x = thumbPositionX, y = thumbPositionY)
            .pointerInteropFilter { false },
        shape = CircleShape,
        color = fillColor.compositeOver(Color.White),
        border = BorderStroke(SliderThumbBorderSize, strokeColor),
        elevation = SliderThumbElevation,
        content = { },
    )
}

private data class HsvColor(
    val hue: Float,
    val saturation: Float,
    val value: Float,
    val alpha: Float,
)

private fun Color.toHsv(): HsvColor {
    val r = this.red
    val g = this.green
    val b = this.blue

    val cMax = max(r, max(g, b))
    val cMin = min(r, min(g, b))
    val diff = cMax - cMin

    val h = when (cMax) {
        cMin -> 0f
        r -> (60 * ((g - b) / diff) + 360) % 360
        g -> (60 * ((b - r) / diff) + 120) % 360
        b -> (60 * ((r - g) / diff) + 240) % 360
        else -> -1f
    }
    val s = if (cMax == 0f) { 0f } else { diff / cMax }

    return HsvColor(h, s, cMax, this.alpha)
}

/**
 * Remembers a [JetPrefColorPickerState] and initializes it with [initColor].
 *
 * @since 0.1.0
 */
@Composable
fun rememberJetPrefColorPickerState(initColor: Color) = remember<JetPrefColorPickerState> {
    if (initColor.isSpecified) {
        val (h, s, v, a) = initColor.toHsv()
        JetPrefColorPickerStateImpl(h, s, v, a)
    } else {
        JetPrefColorPickerStateImpl()
    }
}

/**
 * Interface for a color picker state, which holds the [hue], [saturation], [value] and [alpha] properties of a color,
 * while also allowing outside control of the color.
 *
 * @since 0.1.0
 *
 * @see androidx.compose.ui.graphics.Color.hsv
 */
interface JetPrefColorPickerState {
    /**
     * The `hue` property of the state's color (0..360), where 0 is red, 120 is green, and 240 is blue.
     *
     * @since 0.1.0
     */
    var hue: Float

    /**
     * The `saturation` property of the state's color (0..1), where 0 corresponds to no color and 1 to fully saturated.
     *
     * @since 0.1.0
     */
    var saturation: Float

    /**
     * The `value` property of the state's color (0..1), where 0 is black.
     *
     * @since 0.1.0
     */
    var value: Float

    /**
     * The `alpha` property of the state's color (0..1), where 0 represents fully transparent and 1 fully solid.
     *
     * @since 0.1.0
     */
    var alpha: Float

    /**
     * Returns the current color of this state.
     *
     * @since 0.1.0
     */
    @OptIn(ExperimentalGraphicsApi::class)
    fun color(): Color = Color.hsv(hue, saturation, value, alpha)

    /**
     * Returns the current color of this state while remembering it.
     *
     * @since 0.1.0
     */
    @Composable
    fun rememberColor(): Color = remember(hue, saturation, value, alpha) {
        color()
    }

    /**
     * Set the [hue], [saturation], [value] and [alpha] properties of this state using provided [color].
     *
     * @since 0.1.0
     */
    fun setColor(color: Color) {
        val (h, s, v, a) = color.toHsv()
        hue = h
        saturation = s
        value = v
        alpha = a
    }
}

private fun JetPrefColorPickerState.setHue(pos: Float, length: Float) {
    this.hue = (pos / length).coerceIn(ZeroOneRange) * 360f
}

private fun JetPrefColorPickerState.setSaturation(pos: Float, length: Float) {
    this.saturation = (pos / length).coerceIn(ZeroOneRange)
}

private fun JetPrefColorPickerState.setValue(pos: Float, length: Float) {
    this.value = 1f - (pos / length).coerceIn(ZeroOneRange)
}

private fun JetPrefColorPickerState.setAlpha(pos: Float, length: Float) {
    this.alpha = (pos / length).coerceIn(ZeroOneRange)
}

@OptIn(ExperimentalGraphicsApi::class)
@Composable
private fun JetPrefColorPickerState.rememberHueColor(): Color {
    return remember(this.hue) {
        Color.hsv(this.hue, saturation = 1f, value = 1f)
    }
}

@OptIn(ExperimentalGraphicsApi::class)
@Composable
private fun JetPrefColorPickerState.rememberColorWithoutAlpha(): Color {
    return remember(this.hue, this.saturation, this.value) {
        Color.hsv(this.hue, this.saturation, this.value)
    }
}

private class JetPrefColorPickerStateImpl(
    initHue: Float = 0f,
    initSaturation: Float = 1f,
    initValue: Float = 1f,
    initAlpha: Float = 1f,
) : JetPrefColorPickerState {
    override var hue by mutableStateOf(initHue)
    override var saturation by mutableStateOf(initSaturation)
    override var value by mutableStateOf(initValue)
    override var alpha by mutableStateOf(initAlpha)
}

private interface ShaderBase {
    val image: ImageBitmap
}

private class HueBarShader(width: Int, height: Int) : ShaderBase {
    private val hueGradient = LinearGradientShader(
        from = Offset.Zero,
        to = Offset(x = width.toFloat(), y = 0f),
        colors = listOf(
            Color.Red,
            Color.Yellow,
            Color.Green,
            Color.Cyan,
            Color.Blue,
            Color.Magenta,
            Color.Red,
        ),
        colorStops = null,
    )

    override val image = ImageBitmap(width, height).also { imageBitmap ->
        val canvas = Canvas(imageBitmap)
        val paint = Paint().apply { shader = hueGradient }
        canvas.drawRect(
            left = 0f,
            top = 0f,
            right = width.toFloat(),
            bottom = height.toFloat(),
            paint = paint,
        )
    }
}

private class AlphaBarShader(width: Int, height: Int, colorWithoutAlpha: Color) : ShaderBase {
    private val alphaGradient = LinearGradientShader(
        from = Offset.Zero,
        to = Offset(x = width.toFloat(), y = 0f),
        colors = listOf(
            Color.Transparent,
            colorWithoutAlpha,
        ),
        colorStops = null,
    )

    override val image = ImageBitmap(width, height).also { imageBitmap ->
        val canvas = Canvas(imageBitmap)
        val paint = Paint().apply { shader = alphaGradient }
        canvas.drawRect(
            left = 0f,
            top = 0f,
            right = width.toFloat(),
            bottom = height.toFloat(),
            paint = paint,
        )
    }
}

private class SaturationValueBoxShader(width: Int, height: Int, hueColor: Color) : ShaderBase {
    private val saturationGradient = LinearGradientShader(
        from = Offset.Zero,
        to = Offset(x = width.toFloat(), y = 0f),
        colors = listOf(
            Color.White,
            hueColor,
        ),
        colorStops = null,
    )

    private val valueGradient = LinearGradientShader(
        from = Offset(x = 0f, y = height.toFloat()),
        to = Offset.Zero,
        colors = listOf(
            Color.Black,
            Color.Transparent,
        ),
        colorStops = null,
    )

    override val image = ImageBitmap(width, height).also { imageBitmap ->
        val canvas = Canvas(imageBitmap)
        val saturationPaint = Paint().apply { shader = saturationGradient }
        canvas.drawRect(
            left = 0f,
            top = 0f,
            right = width.toFloat(),
            bottom = height.toFloat(),
            paint = saturationPaint,
        )
        val valuePaint = Paint().apply { shader = valueGradient }
        canvas.drawRect(
            left = 0f,
            top = 0f,
            right = width.toFloat(),
            bottom = height.toFloat(),
            paint = valuePaint,
        )
    }
}
