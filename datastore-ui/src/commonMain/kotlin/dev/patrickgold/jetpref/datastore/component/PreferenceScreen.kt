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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import dev.patrickgold.jetpref.datastore.ui.LocalFlashModifierProvider
import dev.patrickgold.jetpref.datastore.ui.LocalIconSpaceReserved
import dev.patrickgold.jetpref.datastore.ui.LocalPreferenceComponentIdToHighlight
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

abstract class PreferenceScreen(builder: PreferenceScreenBuilder) : Presentable {
    override val title = builder.title
    override val summary = builder.summary
    override val icon = builder.icon

    val components: List<PreferenceComponent> = builder.components?.toList() ?: emptyList()

    val content: @Composable (Modifier) -> Unit

    init {
        components
        content = builder.content ?: @Composable { modifier ->
            val componentIdToHighlight = LocalPreferenceComponentIdToHighlight.current
            val provideFlashModifier = LocalFlashModifierProvider.current
            val lazyListState = rememberLazyListState()
            LaunchedEffect(componentIdToHighlight) {
                val index = this@PreferenceScreen.components.indexOfFirst { it.id == componentIdToHighlight }
                if (index == -1) {
                    return@LaunchedEffect
                }
                lazyListState.animateScrollToItem(index)
            }
            LazyColumn(modifier, state = lazyListState) {
                items(components, key = { it.id }) { component ->
                    Box {
                        component.Render()
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .then(provideFlashModifier(component))
                        )
                    }
                }
            }
        }
    }

    constructor(
        block: PreferenceScreenBuilder.() -> Unit,
    ) : this(PreferenceScreenBuilder().also { it.block() })

    @Composable
    open fun Render() {
        content(Modifier)
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
            Render()
        }
    }
}

fun Modifier.flash(
    component: PreferenceComponent,
    initialDelay: Long = 300L,
    visibleFor: Long = 1000L,
    flashColor: Color = Color.Unspecified,
) = composed {
    val componentIdToHighlight = LocalPreferenceComponentIdToHighlight.current
    var highlightedId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(componentIdToHighlight) {
        delay(initialDelay.milliseconds)
        highlightedId = componentIdToHighlight
        delay(visibleFor.milliseconds)
        highlightedId = null
    }
    val isHighlighted = component.id == highlightedId
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isHighlighted -> flashColor.takeOrElse { MaterialTheme.colorScheme.primary.copy(0.5f) }
            else -> Color.Transparent
        },
        animationSpec = tween(600),
    )
    return@composed Modifier.background(backgroundColor)
}
