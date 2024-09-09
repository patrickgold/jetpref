/*
 * Copyright 2021-2024 Patrick Goldinger
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

package dev.patrickgold.jetpref.material.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * Composable function to only execute the given composable function if the given string is not null or blank.
 * This is useful for conditional composable functions.
 *
 * @param string The string to check for null or blank.
 * @param composer The composable function to execute if the string is not null or blank.
 *
 * @return The composable function or null.
 *
 * @since 0.2.0
 */
@Composable
inline fun whenNotNullOrBlank(
    string: String?,
    crossinline composer: @Composable (text: String) -> Unit,
): @Composable (() -> Unit)? {
    return when {
        !string.isNullOrBlank() -> ({ composer(string) })
        else -> null
    }
}

/**
 * Composable function to only execute the given composable function if the given object is not null.
 * This is useful for conditional composable functions.
 *
 * @param obj The object to check for null.
 * @param composer The composable function to execute if the object is not null.
 *
 * @return The composable function or null.
 *
 * @since 0.2.0
 */
@Composable
inline fun <T : Any> whenNotNull(
    obj: T?,
    crossinline composer: @Composable (obj: T) -> Unit,
): @Composable (() -> Unit)? {
    return when {
        obj != null -> ({ composer(obj) })
        else -> null
    }
}

/**
 * Copies the padding values and applies the given values if they are not null.
 *
 * @param start The start padding value to apply if not null.
 * @param top The top padding value to apply if not null.
 * @param end The end padding value to apply if not null.
 * @param bottom The bottom padding value to apply if not null.
 *
 * @return The new [PaddingValues] with the applied padding values.
 *
 * @since 0.2.0
 */
fun PaddingValues.copy(
    start: Dp? = null,
    top: Dp? = null,
    end: Dp? = null,
    bottom: Dp? = null,
): PaddingValues {
    require(this::class != PaddingValues::Absolute::class) {
        "Cannot copy absolute padding values with this helper."
    }
    // We can force LTR here because we are only copying and not in the layout process
    return PaddingValues(
        start = start ?: this.calculateLeftPadding(LayoutDirection.Ltr),
        top = top ?: this.calculateTopPadding(),
        end = end ?: this.calculateRightPadding(LayoutDirection.Ltr),
        bottom = bottom ?: this.calculateBottomPadding(),
    )
}
