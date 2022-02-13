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

package dev.patrickgold.jetpref.example.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.patrickgold.jetpref.datastore.ui.ScrollablePreferenceLayout
import dev.patrickgold.jetpref.example.examplePreferenceModel
import dev.patrickgold.jetpref.material.ui.ExperimentalJetPrefMaterialUi
import dev.patrickgold.jetpref.material.ui.JetPrefColorPicker
import dev.patrickgold.jetpref.material.ui.checkeredBackground
import dev.patrickgold.jetpref.material.ui.rememberJetPrefColorPickerState

@OptIn(ExperimentalJetPrefMaterialUi::class)
@Composable
fun ColorPickerDemoScreen() = ScrollablePreferenceLayout(examplePreferenceModel()) {
    var color by remember { mutableStateOf(Color.White) }
    Column(modifier = Modifier.padding(all = 32.dp)) {
        val colorPickerState = rememberJetPrefColorPickerState(initColor = color)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .checkeredBackground(),
            color = color,
        ) {
            Text(text = "Color state outside the picker.")
        }

        Row {
            TextButton(onClick = {
                colorPickerState.setColor(Color.Red)
                color = Color.Red
            }) {
                Text(text = "Red")
            }

            TextButton(onClick = {
                colorPickerState.setColor(Color.Green)
                color = Color.Green
            }) {
                Text(text = "Green")
            }

            TextButton(onClick = {
                colorPickerState.setColor(Color.Blue)
                color = Color.Blue
            }) {
                Text(text = "Blue")
            }
        }

        JetPrefColorPicker(
            state = colorPickerState,
            onColorChange = { color = it },
        )
    }
}
