/*
 * Copyright 2025 Patrick Goldinger
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

import androidx.compose.ui.graphics.Color
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class ColorRepresentation(val regex: Regex) {
    HEX(regex = RegexHelpers.HEX),
    RGB(regex = RegexHelpers.RGB),
    HSV(regex = RegexHelpers.HSV);

    fun formatColor(color: Color, withAlpha: Boolean): String {
        return when (this) {
            HEX -> {
                val (r, g, b, a) = color.toRgb()
                val components = if (withAlpha) listOf(r, g, b, (a * 255).roundToInt()) else listOf(r, g, b)
                "#" + components.joinToString("") {
                    String.format(Locale.ROOT, "%02x", it)
                }
            }
            RGB -> {
                val (r, g, b, a) = color.toRgb()
                if (withAlpha) {
                    val alphaStr = String.format(Locale.ROOT, "%.2f", a).trimEnd('0').trimEnd('.')
                    "rgba($r, $g, $b, $alphaStr)"
                } else {
                    "rgb($r, $g, $b)"
                }
            }
            HSV -> {
                val (h, s, v, a) = color.toHsv()
                val hsv = listOf(h.roundToInt(), (s * 100).roundToInt(), (v * 100).roundToInt()).joinToString(", ")
                if (withAlpha) {
                    val alphaStr = String.format(Locale.ROOT, "%.2f", a).trimEnd('0').trimEnd('.')
                    "hsva($hsv, $alphaStr)"
                } else {
                    "hsv($hsv)"
                }
            }
        }
    }

    @Suppress("ConstPropertyName")
    private object RegexHelpers {
        private const val HexDigit = "[0-9a-fA-F]"
        private const val Separator = "\\s*,\\s*"
        private const val ParenOpen = "\\(\\s*"
        private const val ParenClose = "\\s*\\)"
        private const val Float1Dot0 = "1(?:\\.0)?|0(?:\\.[0-9]+)?|\\.[0-9]+"
        private const val Int255 = "25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9]"
        private const val Rgb = "(?<r>$Int255)$Separator(?<g>$Int255)$Separator(?<b>$Int255)(?:$Separator(?<a>$Float1Dot0))?"
        private const val Int360 = "360|3[0-5][0-9]|[1-2][0-9][0-9]|[1-9]?[0-9]"
        private const val Int100 = "100|[1-9]?[0-9]"
        private const val Hsv = "(?<h>$Int360)$Separator(?<s>$Int100)$Separator(?<v>$Int100)(?:$Separator(?<a>$Float1Dot0))?"

        val HEX = "^#(?<r>$HexDigit{2})(?<g>$HexDigit{2})(?<b>$HexDigit{2})(?<a>$HexDigit{2})?\$".toRegex()
        val RGB = "^[rR][gG][bB][aA]?$ParenOpen$Rgb$ParenClose\$".toRegex()
        val HSV = "^[hH][sS][vV][aA]?$ParenOpen$Hsv$ParenClose\$".toRegex()
    }
}

internal data class HsvColor(
    val hue: Float,
    val saturation: Float,
    val value: Float,
    val alpha: Float,
)

internal fun Color.toHsv(): HsvColor {
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

internal data class RgbColor(
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Float,
)

internal fun Color.toRgb(): RgbColor {
    return RgbColor(
        red = (this.red * 255).roundToInt(),
        green = (this.green * 255).roundToInt(),
        blue = (this.blue * 255).roundToInt(),
        alpha = this.alpha,
    )
}

fun Color.Companion.parseOrNull(str: String, withAlpha: Boolean): Pair<Color, ColorRepresentation>? {
    val trimmed = str.trim()

    for (representation in ColorRepresentation.entries) {
        val regex = representation.regex
        val match = regex.matchEntire(trimmed) ?: continue
        if (!withAlpha && match.groups["a"] != null) break
        when (representation) {
            ColorRepresentation.HEX -> {
                val r = match.groups["r"]!!.value.toInt(16) / 255f
                val g = match.groups["g"]!!.value.toInt(16) / 255f
                val b = match.groups["b"]!!.value.toInt(16) / 255f
                val a = match.groups["a"]?.value?.toInt(16)?.div(255f) ?: 1f
                return Color(r, g, b, a) to representation
            }
            ColorRepresentation.RGB -> {
                val r = match.groups["r"]!!.value.toInt() / 255f
                val g = match.groups["g"]!!.value.toInt() / 255f
                val b = match.groups["b"]!!.value.toInt() / 255f
                val a = match.groups["a"]?.value?.toFloat() ?: 1f
                return Color(r, g, b, a) to representation
            }
            ColorRepresentation.HSV -> {
                val h = match.groups["h"]!!.value.toFloat()
                val s = match.groups["s"]!!.value.toFloat() / 100f
                val v = match.groups["v"]!!.value.toFloat() / 100f
                val a = match.groups["a"]?.value?.toFloat() ?: 1f
                return hsv(h, s, v, a) to representation
            }
        }
    }

    return null
}
