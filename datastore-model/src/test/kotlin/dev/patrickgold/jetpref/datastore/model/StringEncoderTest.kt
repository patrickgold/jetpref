package dev.patrickgold.jetpref.datastore.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StringEncoderTest {
    private val stringsToTest = listOf(
        "test" to "\"test\"",
        "\r\n" to "\"\\r\\n\"",
        "\\" to "\"\\\\\"",
        "A sentence with spaces, a comma and a period." to "\"A sentence with spaces, a comma and a period.\"",
        "key=\"value\"" to "\"key=\\\"value\\\"\"",
        "hello\nworld!" to "\"hello\\nworld!\"",
        "\\\"one\\\" two=\"hello\"" to "\"\\\\\\\"one\\\\\\\" two=\\\"hello\\\"\"",
    )

    @Test
    fun `test string encode`() {
        stringsToTest.forEach { (original, encoded) ->
            assertEquals(encoded, StringEncoder.encode(original))
        }
    }

    @Test
    fun `test string decode`() {
        stringsToTest.forEach { (original, encoded) ->
            assertEquals(original, StringEncoder.decode(encoded))
        }
    }
}
