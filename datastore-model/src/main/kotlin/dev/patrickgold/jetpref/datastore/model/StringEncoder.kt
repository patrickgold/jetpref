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

package dev.patrickgold.jetpref.datastore.model

internal object StringEncoder {
    fun encode(str: String): String {
        val sb = StringBuilder()
        sb.append("\"")
        sb.append(str
            .replace("\\", "\\\\")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\"", "\\\"")
        )
        sb.append("\"")
        return sb.toString()
    }

    fun decode(str: String): String {
        val trimmedStr = str.trim()
        return if (trimmedStr.startsWith("\"") && trimmedStr.endsWith("\"") && trimmedStr.length >= 2) {
            trimmedStr
                .substring(1, trimmedStr.length - 1)
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\\\", "\\")
        } else {
            ""
        }
    }
}
