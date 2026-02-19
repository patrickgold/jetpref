package dev.patrickgold.jetpref.example.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.patrickgold.jetpref.datastore.component.PreferenceScreen
import dev.patrickgold.jetpref.datastore.ui.PreferenceSearch
import dev.patrickgold.jetpref.example.ExamplePreferenceComponentTree

data object SearchScreen : PreferenceScreen({
    title { "Search" }

    content {
        var searchText by remember { mutableStateOf("") }
        PreferenceSearch(
            modifier = Modifier.fillMaxSize(),
            searchIndex = remember {
                ExamplePreferenceComponentTree.buildSearchIndex()
            },
            searchText = searchText,
            beforeResultsContent = {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = searchText,
                    onValueChange = { searchText = it },
                )
            },
        )
    }
})
