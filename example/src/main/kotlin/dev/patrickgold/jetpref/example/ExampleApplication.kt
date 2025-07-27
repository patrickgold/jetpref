package dev.patrickgold.jetpref.example

import android.app.Application
import dev.patrickgold.jetpref.datastore.runtime.init
import kotlinx.coroutines.runBlocking

@Suppress("unused")
class ExampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize your datastore here (required)
        runBlocking {
            AppPrefsStore.init(
                context = this@ExampleApplication,
                datastoreName = "example-app-preferences",
            )
                .onSuccess { println("loaded model successfully") }
                .onFailure { println("error occurred: $it") }
        }
    }
}
