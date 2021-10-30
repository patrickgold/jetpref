package dev.patrickgold.jetpref.example

import android.app.Application

class ExampleApplication : Application() {
    private val prefs by examplePreferenceModel()

    override fun onCreate() {
        super.onCreate()
        prefs.initializeForContext(this)
    }
}
