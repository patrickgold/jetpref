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

abstract class PreferenceComponentTree {
    abstract val mainEntryPoint: PreferencePage

    fun buildSearchIndex(
        options: SearchIndex.Options = SearchIndex.Options.Default,
    ): SearchIndex {
        val entries = mutableListOf<SearchIndex.Entry>()
        val displayPath = listOf<Presentable>(mainEntryPoint)
        buildSearchIndexRecursive(options, entries, displayPath, mainEntryPoint)
        return SearchIndex(options, entries.toList())
    }

    // TODO cyclic graph detection
    private fun buildSearchIndexRecursive(
        options: SearchIndex.Options,
        entries: MutableList<SearchIndex.Entry>,
        displayPath: List<Presentable>,
        page: PreferencePage,
    ) {
        for (component in page.components) {
            val displayPath = when {
                component.associatedGroup != null && options.includeGroupsInDisplayPath -> {
                    displayPath.plus(component.associatedGroup!!)
                }
                else -> {
                    displayPath
                }
            }
            when {
                component is PreferenceComponent.LinkedPage -> {
                    entries.add(SearchIndex.Entry(component, page, displayPath))
                    buildSearchIndexRecursive(options, entries, displayPath.plus(component.targetPage), component.targetPage)
                }
                component.searchPolicy is SearchPolicy.AlwaysExclude -> {
                    // exclude from indexing
                }
                else -> {
                    entries.add(SearchIndex.Entry(component, page, displayPath))
                }
            }
        }
    }
}
