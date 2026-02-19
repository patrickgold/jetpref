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

package dev.patrickgold.jetpref.datastore.component

import androidx.compose.runtime.Composable

abstract class PreferenceComponentTree {
    abstract val mainEntryPoint: PreferenceScreen

    fun buildSearchIndex(options: SearchIndexOptions = SearchIndexOptions.Default): SearchIndex {
        val entries = mutableListOf<SearchIndexEntry>()
        val displayPath = emptyList<@Composable () -> String>()
        buildSearchIndexRecursive(options, entries, displayPath, mainEntryPoint)
        return SearchIndex(options, entries.toList())
    }

    private fun buildSearchIndexRecursive(
        options: SearchIndexOptions,
        entries: MutableList<SearchIndexEntry>,
        displayPath: List<@Composable () -> String>,
        screen: PreferenceScreen,
    ) {
        for (component in screen.components) {
            when (component) {
                is PreferenceComponent.NavigationEntry -> {
                    entries.add(SearchIndexEntry(component, screen, displayPath))
                    buildSearchIndexRecursive(options, entries, displayPath.plus(component.targetScreen.title), component.targetScreen)
                }
                is PreferenceComponent.ComposableContent -> {
                    // exclude from indexing
                }
                else -> {
                    entries.add(SearchIndexEntry(component, screen, displayPath))
                }
            }
        }
    }

    data class SearchIndex(
        val options: SearchIndexOptions,
        val entries: List<SearchIndexEntry>,
    )

    data class SearchIndexOptions(
        val includeGroupsInDisplayPath: Boolean = true,
    ) {
        companion object {
            val Default = SearchIndexOptions()
        }
    }

    data class SearchIndexEntry(
        val component: PreferenceComponent,
        val associatedScreen: PreferenceScreen,
        val displayPath: List<@Composable () -> String>,
    )
}
