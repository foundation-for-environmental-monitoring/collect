@file:Suppress("DEPRECATION")

package io.ffem.collect.android.util

import androidx.test.core.app.ApplicationProvider
import androidx.test.uiautomator.UiDevice

lateinit var mDevice: UiDevice

object TestHelper {
    fun clearPreferences() {
        val prefs =
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
        prefs.edit().clear().apply()
    }

    fun isDeviceInitialized(): Boolean {
        return ::mDevice.isInitialized
    }
}