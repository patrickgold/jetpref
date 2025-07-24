package dev.patrickgold.jetpref.example

import android.app.Application
import dev.patrickgold.jetpref.datastore.JetPref
import dev.patrickgold.jetpref.datastore.init
import kotlinx.coroutines.runBlocking

@Suppress("unused")
class ExampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Optionally initialize global JetPref configs. This must be done before
        // any preference datastore is initialized!
        JetPref.configure(
            saveIntervalMs = 500,
            encodeDefaultValues = true,
        )

        // Initialize your datastore here (required)
        runBlocking {
            AppPrefs.init(
                context = this@ExampleApplication,
                datastoreName = "example-app-preferences",
            )
                .onSuccess { println("loaded model successfully") }
                .onFailure { println("error occurred: $it") }
        }
    }
}
