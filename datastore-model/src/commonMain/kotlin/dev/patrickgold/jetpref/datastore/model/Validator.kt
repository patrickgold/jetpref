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

internal object Validator {
    private val FILE_NAME_REGEX = """^(([a-zA-Z_])|([a-zA-Z_][a-zA-Z0-9_-]*[a-zA-Z0-9_]))${'$'}""".toRegex()
    private val KEY_REGEX = """^(([a-zA-Z_])|([a-zA-Z_][a-zA-Z0-9_-]*[a-zA-Z0-9_]))${'$'}""".toRegex()

    fun validateFileName(fileName: String): String {
        if (!FILE_NAME_REGEX.matches(fileName)) {
            throw IllegalArgumentException(
                "Datastore file name '$fileName' does not conform to the expected format of $FILE_NAME_REGEX"
            )
        }
        return fileName
    }

    fun validateKey(key: String): String {
        if (!KEY_REGEX.matches(key)) {
            throw IllegalArgumentException(
                "Preference key '$key' does not conform to the expected format of $KEY_REGEX"
            )
        }
        return key
    }
}
