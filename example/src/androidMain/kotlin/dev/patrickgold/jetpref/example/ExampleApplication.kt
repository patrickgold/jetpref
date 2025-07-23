package dev.patrickgold.jetpref.example

import android.app.Application
import dev.patrickgold.jetpref.datastore.JetPref

@Suppress("unused")
class ExampleApplication : Application() {
    private val prefs by examplePreferenceModel()

    override fun onCreate() {
        super.onCreate()

        // Optionally initialize global JetPref configs. This must be done before
        // any preference datastore is initialized!
        JetPref.configure(
            saveIntervalMs = 500,
            encodeDefaultValues = true,
        )

        // Initialize your datastore here (required)
        prefs.initializeBlocking(this)
    }
}
