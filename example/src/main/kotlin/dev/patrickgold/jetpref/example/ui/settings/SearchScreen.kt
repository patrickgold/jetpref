package dev.patrickgold.jetpref.example.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.patrickgold.jetpref.datastore.ui.PreferenceSearch

@Composable
fun SearchScreen() {
    var searchText by remember { mutableStateOf("") }
            PreferenceSearch(
            modifier = Modifier.fillMaxSize(),
        searchIndex = remember {
            ExampleComponentTree.buildSearchIndex()
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
