package dev.patrickgold.jetpref.datastore.model

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class StringEncoderTest : FreeSpec({
    val stringsToTest = listOf(
        "test" to "\"test\"",
        "\r\n" to "\"\\r\\n\"",
        "\\" to "\"\\\\\"",
        "A sentence with spaces, a comma and a period." to "\"A sentence with spaces, a comma and a period.\"",
        "key=\"value\"" to "\"key=\\\"value\\\"\"",
        "hello\nworld!" to "\"hello\\nworld!\"",
        "\\\"one\\\" two=\"hello\"" to "\"\\\\\\\"one\\\\\\\" two=\\\"hello\\\"\"",
    )

    "String encode/decode" - {
        "Test string encode" - {
            stringsToTest.forEach { (original, encoded) ->
                "`$original` should be `$encoded`" {
                    StringEncoder.encode(original) shouldBe encoded
                }
            }
        }
        "Test string decode" - {
            stringsToTest.forEach { (original, encoded) ->
                "`$encoded` should be `$original`" {
                    StringEncoder.decode(encoded) shouldBe original
                }
            }
        }
    }
})
