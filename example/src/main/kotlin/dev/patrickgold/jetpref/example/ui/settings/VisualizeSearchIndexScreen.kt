package dev.patrickgold.jetpref.example.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.patrickgold.jetpref.datastore.component.PreferenceScreen
import dev.patrickgold.jetpref.example.ExamplePreferenceComponentTree

data object VisualizeSearchIndexScreen : PreferenceScreen({
    title { "Visualize search index screen" }

    content {
        val searchIndex = remember {
            ExamplePreferenceComponentTree.buildSearchIndex()
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(searchIndex.options.toString())
            }
            items(searchIndex.entries) { entry ->
                Text(entry.toString())
            }
        }
    }
})
