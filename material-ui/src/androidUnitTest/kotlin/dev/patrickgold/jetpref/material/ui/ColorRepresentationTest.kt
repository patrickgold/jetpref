package dev.patrickgold.jetpref.material.ui

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private data class ColorRow(
    val color: Color,
    val hex: String,
    val rgb: String,
    val hsv: String,
)

class ColorRepresentationTest {
    private fun rgba(rgba: Long): Color {
        val r = ((rgba shr 24) and 0xff).toFloat() / 255
        val g = ((rgba shr 16) and 0xff).toFloat() / 255
        val b = ((rgba shr 8) and 0xff).toFloat() / 255
        val a = (rgba and 0xff).toFloat() / 255
        return Color(r, g, b, a)
    }

    private fun rgb(rgb: Long): Color {
        val r = ((rgb shr 16) and 0xff).toFloat() / 255
        val g = ((rgb shr 8) and 0xff).toFloat() / 255
        val b = (rgb and 0xff).toFloat() / 255
        return Color(r, g, b)
    }

    private val colorTable = listOf(
        ColorRow(
            color = rgb(0xffffff),
            hex = "#ffffff",
            rgb = "rgb(255, 255, 255)",
            hsv = "hsv(0, 0, 100)",
        ),
        ColorRow(
            color = rgb(0x000000),
            hex = "#000000",
            rgb = "rgb(0, 0, 0)",
            hsv = "hsv(0, 0, 0)",
        ),
        ColorRow(
            color = rgb(0x7f7f7f),
            hex = "#7f7f7f",
            rgb = "rgb(127, 127, 127)",
            hsv = "hsv(0, 0, 50)",
        ),
        ColorRow(
            color = rgb(0x123456),
            hex = "#123456",
            rgb = "rgb(18, 52, 86)",
            hsv = "hsv(210, 79, 34)",
        ),
        ColorRow(
            color = rgb(0xabcdef),
            hex = "#abcdef",
            rgb = "rgb(171, 205, 239)",
            hsv = "hsv(210, 28, 94)",
        ),
        ColorRow(
            color = rgb(0x57992c),
            hex = "#57992c",
            rgb = "rgb(87, 153, 44)",
            hsv = "hsv(96, 71, 60)",
        ),
        ColorRow(
            color = rgb(0xaabde2),
            hex = "#aabde2",
            rgb = "rgb(170, 189, 226)",
            hsv = "hsv(220, 25, 89)",
        ),
        ColorRow(
            color = rgb(0x210f18),
            hex = "#210f18",
            rgb = "rgb(33, 15, 24)",
            hsv = "hsv(330, 55, 13)",
        ),
        ColorRow(
            color = rgb(0xa02784),
            hex = "#a02784",
            rgb = "rgb(160, 39, 132)",
            hsv = "hsv(314, 76, 63)",
        ),
    )

    @Test
    fun `test HEX color formatting without alpha`() {
        colorTable.forEach { (color, expected, _, _) ->
            assertEquals(expected, ColorRepresentation.HEX.formatColor(color, withAlpha = false))
        }
    }

    @Test
    fun `test RGB color formatting without alpha`() {
        colorTable.forEach { (color, _, expected, _) ->
            assertEquals(expected, ColorRepresentation.RGB.formatColor(color, withAlpha = false))
        }
    }

    @Test
    fun `test HSV color formatting without alpha`() {
        colorTable.forEach { (color, _, _, expected) ->
            assertEquals(expected, ColorRepresentation.HSV.formatColor(color, withAlpha = false))
        }
    }

    @Test
    fun `test HEX color parsing without alpha`() {
        for (r in 0..255) {
            val rStr = String.format("%02x", r)
            for (g in 0..255) {
                val gStr = String.format("%02x", g)
                for (b in 0..255) {
                    val bStr = String.format("%02x", b)
                    val expectedColor = Color(r / 255f, g / 255f, b / 255f)
                    val hexStr = "#$rStr$gStr$bStr"
                    val result = Color.parseOrNull(hexStr, withAlpha = false)
                    assertNotNull(result, "Failed to parse $hexStr")
                    val (actualColor, representation) = result
                    assertEquals(expectedColor, actualColor)
                    assertEquals(ColorRepresentation.HEX, representation)
                }
            }
        }
    }

    @Test
    fun `test RGB color parsing without alpha`() {
        for (r in 0..255) {
            for (g in 0..255) {
                for (b in 0..255) {
                    val expectedColor = Color(r / 255f, g / 255f, b / 255f)
                    val rgbStr = "rgb($r, $g, $b)"
                    val result = Color.parseOrNull(rgbStr, withAlpha = false)
                    assertNotNull(result, "Failed to parse $rgbStr")
                    val (actualColor, representation) = result
                    assertEquals(expectedColor, actualColor)
                    assertEquals(ColorRepresentation.RGB, representation)
                }
            }
        }
    }

    @Test
    fun `test HSV color parsing without alpha`() {
        for (h in 0..<360) {
            for (s in 0..100) {
                for (v in 0..100) {
                    val expectedColor = Color.hsv(h.toFloat(), s.toFloat() / 100f, v.toFloat() / 100f)
                    val hsvStr = "hsv($h, $s, $v)"
                    val result = Color.parseOrNull(hsvStr, withAlpha = false)
                    assertNotNull(result, "Failed to parse $hsvStr")
                    val (actualColor, representation) = result
                    assertEquals(expectedColor, actualColor)
                    assertEquals(ColorRepresentation.HSV, representation)
                }
            }
        }
    }

    @Test
    fun `text HEX color parsing with alpha`() {
        for (r in 0..255 step 4) {
            val rStr = String.format("%02x", r)
            for (g in 0..255 step 4) {
                val gStr = String.format("%02x", g)
                for (b in 0..255 step 4) {
                    val bStr = String.format("%02x", b)
                    for (a in 0..255 step 64) {
                        val aStr = String.format("%02x", a)
                        val expectedColor = Color(r / 255f, g / 255f, b / 255f, a / 255f)
                        val hexStr = "#$rStr$gStr$bStr$aStr"
                        val result = Color.parseOrNull(hexStr, withAlpha = true)
                        assertNotNull(result, "Failed to parse $hexStr")
                        val (actualColor, representation) = result
                        assertEquals(expectedColor, actualColor)
                        assertEquals(ColorRepresentation.HEX, representation)
                    }
                }
            }
        }
    }

    @Test
    fun `test RGB color parsing with alpha`() {
        for (r in 0..255 step 4) {
            for (g in 0..255 step 4) {
                for (b in 0..255 step 4) {
                    for (a in 0..255 step 64) {
                        val expectedColor = Color(r / 255f, g / 255f, b / 255f, a / 255f)
                        val rgbStr = "rgb($r, $g, $b, ${a / 255f})"
                        val result = Color.parseOrNull(rgbStr, withAlpha = true)
                        assertNotNull(result, "Failed to parse $rgbStr")
                        val (actualColor, representation) = result
                        assertEquals(expectedColor, actualColor)
                        assertEquals(ColorRepresentation.RGB, representation)
                    }
                }
            }
        }
    }

    @Test
    fun `test HSV color parsing with alpha`() {
        for (h in 0..<360 step 4) {
            for (s in 0..100 step 4) {
                for (v in 0..100 step 4) {
                    for (a in 0..255 step 64) {
                        val expectedColor = Color.hsv(h.toFloat(), s.toFloat() / 100f, v.toFloat() / 100f, a / 255f)
                        val hsvStr = "hsv($h, $s, $v, ${a / 255f})"
                        val result = Color.parseOrNull(hsvStr, withAlpha = true)
                        assertNotNull(result, "Failed to parse $hsvStr")
                        val (actualColor, representation) = result
                        assertEquals(expectedColor, actualColor)
                        assertEquals(ColorRepresentation.HSV, representation)
                    }
                }
            }
        }
    }

    @Test
    fun `test color parsing for invalid strings`() {
        val invalidStrings = listOf(
            "",
            "#test12",
            "rgb(-2, 255, 255)",
            "rgb(256, 255, 255)",
            "rgb(300, 255, 255)",
            "rgb(255, -2, 255)",
            "rgb(0 0 0)",
            "hsv(389, 100, 100)",
            "hsv(0, 101, 100)",
        )
        invalidStrings.forEach { str ->
            val result = Color.parseOrNull(str, withAlpha = false)
            assertNull(result, "Expected parsing fail for $str")
        }
    }
}
