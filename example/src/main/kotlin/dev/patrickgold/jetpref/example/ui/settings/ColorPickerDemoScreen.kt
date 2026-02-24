package dev.patrickgold.jetpref.example.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import dev.patrickgold.jetpref.material.ui.ExperimentalJetPrefMaterial3Ui
import dev.patrickgold.jetpref.material.ui.JetPrefColorPicker
import dev.patrickgold.jetpref.material.ui.checkeredBackground
import dev.patrickgold.jetpref.material.ui.rememberJetPrefColorPickerState

@OptIn(ExperimentalJetPrefMaterial3Ui::class)
@Composable
fun ColorPickerDemoScreen() {
    var color by remember { mutableStateOf(Color.Red) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 16.dp),
    ) {
        val colorPickerState = rememberJetPrefColorPickerState(initColor = color)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .checkeredBackground(),
            color = color,
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Color state outside the picker.",
                color = if (ColorUtils.calculateLuminance(color.toArgb()) > 0.179f) Color.Black else Color.White,
            )
        }

        JetPrefColorPicker(
            state = colorPickerState,
            onColorChange = { color = it },
        )
    }
}
