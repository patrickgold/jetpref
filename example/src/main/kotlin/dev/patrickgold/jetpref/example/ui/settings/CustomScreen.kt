package dev.patrickgold.jetpref.example.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import dev.patrickgold.jetpref.datastore.component.PreferenceScreen
import dev.patrickgold.jetpref.datastore.component.PreferenceScreenBuilder
import dev.patrickgold.jetpref.example.R

class CustomPrefScreenBuilder : PreferenceScreenBuilder() {
    var fab: @Composable () -> Unit = { }
        private set

    fun floatingActionButton(fab: @Composable () -> Unit) {
        this.fab = fab
    }
}

abstract class CustomPrefScreen(
    builder: CustomPrefScreenBuilder,
) : PreferenceScreen(builder) {
    val fab = builder.fab

    constructor(
        block: CustomPrefScreenBuilder.() -> Unit,
    ) : this(CustomPrefScreenBuilder().also { it.block() })

    @Composable
    override fun Render() {
        Scaffold(floatingActionButton = fab) { innerPadding ->
            content(Modifier.padding(innerPadding))
        }
    }
}

data object CustomScreen : CustomPrefScreen({
    title { "Custom screen" }

    components {
        content {
            Text("Placeholder")
        }
    }

    floatingActionButton {
        FloatingActionButton({}) {
            Icon(painterResource(R.drawable.ic_palette), null)
        }
    }
})
