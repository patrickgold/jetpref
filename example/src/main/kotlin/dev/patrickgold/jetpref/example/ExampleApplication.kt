package dev.patrickgold.jetpref.example

import android.app.Application

@Suppress("unused")
class ExampleApplication : Application() {
    private val prefs by examplePreferenceModel()

    override fun onCreate() {
        super.onCreate()
        prefs.initializeBlocking(this)
    }
}
