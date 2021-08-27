package dev.patrickgold.jetpref.datastore

import android.content.Context
import android.content.SharedPreferences
import java.lang.ref.WeakReference

abstract class JetPrefDataStore(private val name: String) {
    companion object {
        private var applicationContext: WeakReference<Context> = WeakReference(null)

        fun setContext(context: Context) {
            applicationContext = WeakReference(context.applicationContext ?: context)
        }
    }

    private val shared: SharedPreferences by lazy {
        applicationContext.get()?.getSharedPreferences(name, Context.MODE_PRIVATE)
            ?: throw NullPointerException("Application context reference is null. Either it was not set or the reference has become invalid.")
    }

    private fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return try {
            shared.getBoolean(key, defaultValue)
        } catch (e: Throwable) {
            defaultValue
        }
    }

    private fun getInt(key: String, defaultValue: Int): Int {
        return try {
            shared.getInt(key, defaultValue)
        } catch (e: Throwable) {
            defaultValue
        }
    }

    private fun getString(key: String, defaultValue: String): String {
        return try {
            shared.getString(key, defaultValue)!!
        } catch (e: Throwable) {
            defaultValue
        }
    }

    private fun setBoolean(key: String, value: Boolean) {
        try {
            shared.edit().putBoolean(key, value).apply()
        } catch (e: Throwable) {
        }
    }

    private fun setInt(key: String, value: Int) {
        try {
            shared.edit().putInt(key, value).apply()
        } catch (e: Throwable) {
        }
    }

    private fun setString(key: String, value: String) {
        try {
            shared.edit().putString(key, value).apply()
        } catch (e: Throwable) {
        }
    }

    private inner class BooleanJetPrefData (
        override val key: String,
        override val defaultValue: Boolean,
    ) : JetPrefData<Boolean> {

        override var value: Boolean
            get() =  getBoolean(key, defaultValue)
            set(v) = setBoolean(key, v)
    }

    private inner class IntJetPrefData(
        override val key: String,
        override val defaultValue: Int,
    ) : JetPrefData<Int> {

        override var value: Int
            get() =  getInt(key, defaultValue)
            set(v) = setInt(key, v)
    }

    private inner class StringJetPrefData(
        override val key: String,
        override val defaultValue: String,
    ) : JetPrefData<String> {

        override var value: String
            get() =  getString(key, defaultValue)
            set(v) = setString(key, v)
    }

    private inner class CustomJetPrefData<V : Any>(
        override val key: String,
        override val defaultValue: V,
        val convertFromString: (v: String) -> V,
        val convertToString: (v: V) -> String,
    ) : JetPrefData<V> {

        // Cache converted default value as it never changes
        private val _defaultValue = convertToString(defaultValue)

        override var value: V
            get() =  convertFromString(getString(key, _defaultValue))
            set(v) = setString(key, convertToString(v))
    }

    protected open class PreferenceBuilder<T : Any> {
        lateinit var key: String
        lateinit var defaultValue: T
    }

    protected open class CustomPreferenceBuilder<T : Any> : PreferenceBuilder<T>() {
        lateinit var convertFromString: (v: String) -> T
        lateinit var convertToString: (v: T) -> String
    }

    protected fun boolean(configure: PreferenceBuilder<Boolean>.() -> Unit): JetPrefData<Boolean> {
        val builder = PreferenceBuilder<Boolean>()
        configure(builder)
        return BooleanJetPrefData(builder.key, builder.defaultValue)
    }

    protected fun int(configure: PreferenceBuilder<Int>.() -> Unit): JetPrefData<Int> {
        val builder = PreferenceBuilder<Int>()
        configure(builder)
        return IntJetPrefData(builder.key, builder.defaultValue)
    }

    protected fun string(configure: PreferenceBuilder<String>.() -> Unit): JetPrefData<String> {
        val builder = PreferenceBuilder<String>()
        configure(builder)
        return StringJetPrefData(builder.key, builder.defaultValue)
    }

    protected fun <T : Any> custom(configure: CustomPreferenceBuilder<T>.() -> Unit): JetPrefData<T> {
        val builder = CustomPreferenceBuilder<T>()
        configure(builder)
        return CustomJetPrefData(builder.key, builder.defaultValue,
            builder.convertFromString, builder.convertToString)
    }
}
