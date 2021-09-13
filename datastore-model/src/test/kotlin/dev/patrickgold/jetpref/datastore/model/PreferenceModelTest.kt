package dev.patrickgold.jetpref.datastore.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

class StringEncodeDecodeArgumentsProvider : ArgumentsProvider {
    private val stringsToTest = listOf(
        "test" to "\"test\"",
        "\r\n" to "\"\\r\\n\"",
        "\\" to "\"\\\\\"",
        "A sentence with spaces, a comma and a period." to "\"A sentence with spaces, a comma and a period.\"",
        "key=\"value\"" to "\"key=\\\"value\\\"\"",
        "hello\nworld!" to "\"hello\\nworld!\"",
        "\\\"one\\\" two=\"hello\"" to "\"\\\\\\\"one\\\\\\\" two=\\\"hello\\\"\"",
    ).map { Arguments.of(it.first, it.second) }

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return stringsToTest.stream()
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PreferenceModelTest {
    val preferenceModel = object : PreferenceModel("name") { }

    @DisplayName("String encoding/decoding process")
    @Nested
    inner class StringEncodeDecode {
        @ParameterizedTest
        @ArgumentsSource(StringEncodeDecodeArgumentsProvider::class)
        fun `Test String Encode`(input: String, expectedOutput: String) {
            with(preferenceModel) {
                input.encode() shouldBe expectedOutput
            }
        }

        @ParameterizedTest
        @ArgumentsSource(StringEncodeDecodeArgumentsProvider::class)
        fun `Test String Decode`(expectedOutput: String, input: String) {
            with(preferenceModel) {
                input.decode() shouldBe expectedOutput
            }
        }
    }
}
