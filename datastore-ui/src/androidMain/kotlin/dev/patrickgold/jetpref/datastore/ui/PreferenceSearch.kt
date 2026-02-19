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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.patrickgold.jetpref.datastore.component.SearchIndex
import dev.patrickgold.jetpref.material.ui.JetPrefListItem

@Composable
fun PreferenceSearch(
    searchIndex: SearchIndex,
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
            val displayPath = entry.displayPath.map { it.icon?.invoke() to it.title.invoke() }
            if (searchTextTrimmed.isNotBlank() && title.lowercase().contains(searchTextTrimmed)) {
                JetPrefListItem(
                    modifier = Modifier
                        .clickable {
                            router.navigateTo(entry.associatedScreen, entry.component)
                        },
                    icon = maybeJetIcon(entry.component.icon?.invoke()),
                    headlineContent = { Text(title) },
                    secondaryContent = {
                        Row {
                            displayPath.forEachIndexed { index, (icon, title) ->
                                if (index > 0) {
                                    Text(" > ")
                                }
                                if (icon != null) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                    )
                                }
                                Text(title)
                            }
                        }
                    },
                )
            } else {
                //
            }
        }
        item { afterResultsContent() }
    }
}
