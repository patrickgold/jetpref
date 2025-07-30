/*
 * Copyright 2025 Patrick Goldinger
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

package com.example.tests

import kotlin.test.assertEquals
import kotlin.test.assertTrue

// TODO: this should be provided by kotlin.test (is on jvm, but for some reason not on multiplatform)
internal inline fun <reified T : Throwable> assertThrows(block: () -> Unit): T {
    var exception: Throwable? = null
    try {
        block()
    } catch (e: Throwable) {
        exception = e
    }
    assertEquals(T::class, exception?.let { it::class })
    return exception as T
}

internal fun <T> Result<T>.assertIsSuccess(): T {
    assertTrue(isSuccess, "Expected success, but was failure: ${exceptionOrNull()}")
    return getOrNull()!!
}

internal fun <T> Result<T>.assertIsFailure(): Throwable {
    assertTrue(isFailure, "Expected failure, but was success: ${getOrNull()}")
    return exceptionOrNull()!!
}

internal fun <T> Result<T>.assertIsFailure(expectedThrowable: Throwable): Throwable {
    val actualThrowable = assertIsFailure()
    assertEquals(expectedThrowable, actualThrowable, "Result was failure, but failure objects mismatch")
    return actualThrowable
}
