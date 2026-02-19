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

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.ImageVector
import dev.patrickgold.jetpref.datastore.ui.LocalIconSpaceReserved
import dev.patrickgold.jetpref.datastore.ui.LocalPreferenceComponentIdToHighlight

abstract class PreferenceScreen(block: PreferenceScreenBuilder.() -> Unit) : Presentable {
    private val titleBacking: @Composable () -> String

    override val title: @Composable () -> String
        get() = titleBacking

    private val iconBacking: (@Composable () -> ImageVector)?

    override val icon: (@Composable () -> ImageVector)?
        get() = iconBacking

    val components: List<PreferenceComponent>

    val content: @Composable () -> Unit

    init {
        val builder = PreferenceScreenBuilder(this::class)
        builder.block()
        titleBacking = builder.title
        iconBacking = builder.icon
        components = builder.components?.toList() ?: emptyList()
        content = builder.content ?: @Composable {
            val componentIdToHighlight = LocalPreferenceComponentIdToHighlight.current
            val lazyListState = rememberLazyListState()
            LaunchedEffect(componentIdToHighlight) {
                val index = this@PreferenceScreen.components.indexOfFirst { it.id == componentIdToHighlight }
                if (index == -1) {
                    return@LaunchedEffect
                }
                lazyListState.animateScrollToItem(index)
            }
            LazyColumn(state = lazyListState) {
                items(components, key = { it.id }) { component ->
                    component.Render()
                }
            }
        }
    }

    @Composable
    operator fun invoke(
        componentIdToHighlight: Int = -1,
        iconSpaceReserved: Boolean = LocalIconSpaceReserved.current,
    ) {
        CompositionLocalProvider(
            LocalPreferenceComponentIdToHighlight provides componentIdToHighlight,
            LocalIconSpaceReserved provides iconSpaceReserved,
        ) {
            content()
        }
    }
}
