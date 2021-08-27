package dev.patrickgold.jetpref.datastore

import android.app.Application

open class JetPrefApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        JetPrefDataStore.setContext(this)
    }
}
