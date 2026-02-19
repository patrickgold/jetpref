/*
 * Copyright 2026 Patrick Goldinger
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

package dev.patrickgold.jetpref.datastore.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.patrickgold.jetpref.datastore.component.PreferenceComponentTree

@Composable
fun PreferenceSearch(
    searchIndex: PreferenceComponentTree.SearchIndex,
    searchText: String,
    modifier: Modifier = Modifier,
    beforeResultsContent: @Composable () -> Unit = { },
    afterResultsContent: @Composable () -> Unit = { },
) {
    val router = LocalPreferenceNavigationRouter.current
    val searchTextTrimmed = remember(searchText) { searchText.trim().lowercase() }
    LazyColumn(modifier) {
        item { beforeResultsContent() }
        items(searchIndex.entries, key = { it.component.id }) { entry ->
            val title = entry.component.title.invoke()
            val displayPath = entry.displayPath.map { it.invoke() }
            val displayPathText = remember(displayPath) {
                displayPath.joinToString(" > ")
            }
            if (searchTextTrimmed.isNotBlank() && title.lowercase().contains(searchTextTrimmed)) {
                Preference(
                    title = title,
                    summary = displayPathText,
                    onClick = {
                        router.navigateTo(entry.associatedScreen, entry.component)
                    },
                )
            } else {
                //
            }
        }
        item { afterResultsContent() }
    }
}
