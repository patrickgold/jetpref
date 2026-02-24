package dev.patrickgold.jetpref.example.ui.settings

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ParameterizedScreen(name: String) {
    Button({}) {
        Text("Hello $name")
    }
}
