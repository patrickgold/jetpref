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
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

abstract class PreferencePage(builder: PreferencePageBuilder) : Presentable {
    override val title = builder.title
    override val summary = builder.summary
    override val icon = builder.icon

    val components: List<PreferenceComponent> = builder.components?.toList() ?: emptyList()

    constructor(
        block: PreferencePageBuilder.() -> Unit,
    ) : this(PreferencePageBuilder().also { it.block() })

    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        anchorId: Int = -1,
    ) {
        val provideFlashModifier = LocalFlashModifierProvider.current
        val lazyListState = rememberLazyListState()
        LaunchedEffect(anchorId) {
            val index = this@PreferencePage.components.indexOfFirst { it.id == anchorId }
            if (index == -1) {
                return@LaunchedEffect
            }
            lazyListState.animateScrollToItem(index)
        }
        LazyColumn(modifier, state = lazyListState) {
            items(components, key = { it.id }) { component ->
                Box {
                    component.Content()
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .then(if (component.id == anchorId) {
                                provideFlashModifier()
                            } else Modifier)
                    )
                }
            }
        }
    }
}

fun Modifier.flash(
    initialDelay: Long = 300L,
    visibleFor: Long = 1000L,
    flashColor: Color = Color.Unspecified,
) = composed {
    var isHighlighted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(initialDelay.milliseconds)
        isHighlighted = true
        delay(visibleFor.milliseconds)
        isHighlighted = false
    }
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isHighlighted -> flashColor.takeOrElse { MaterialTheme.colorScheme.primary.copy(0.5f) }
            else -> Color.Transparent
        },
        animationSpec = tween(600),
    )
    return@composed Modifier.background(backgroundColor)
}
