/*
 * Copyright 2021 Patrick Goldinger
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

import android.text.format.DateFormat.is24HourFormat
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.patrickgold.jetpref.datastore.model.LocalTime


@Composable
internal fun maybeJetIcon(
    imageVector: ImageVector?,
    iconSpaceReserved: Boolean,
    contentDescription: String? = null,
): @Composable (() -> Unit)? {
    return when {
        imageVector != null -> ({
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
            )
        })
        iconSpaceReserved -> ({
            Spacer(modifier = Modifier.width(24.dp))
        })
        else -> null
    }
}


val LocalTime.isAfternoon
    get() = hour >= 12

@get:Composable
val LocalTime.hourForDisplay: Int
    get() {
        return when {
            is24HourFormat(LocalContext.current) -> hour % 24
            hour % 12 == 0 -> 12
            isAfternoon -> hour - 12
            else -> hour
        }
    }

@get:Composable
val LocalTime.stringRepresentation: String
    get() {
        return if (is24HourFormat(LocalContext.current)) {
            "$hourForDisplay:${String.format("%02d", minute)}"
        } else {
            if (isAfternoon) {
                "$hourForDisplay:${String.format("%02d", minute)} PM"
            } else {
                "$hourForDisplay:${String.format("%02d", minute)} AM"
            }
        }
    }
