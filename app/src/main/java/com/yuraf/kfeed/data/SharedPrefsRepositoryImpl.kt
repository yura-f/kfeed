package com.yuraf.kfeed.data

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * @author Yura F (yura-f.github.io)
 */
class SharedPrefsRepositoryImpl(private val sharedPreferences: SharedPreferences) : SharedPrefsRepository {
    companion object {
        const val KEY_LOCATION_SERVICE_ENABLED = "key_location_service_enabled"
    }

    override fun isLocationServiceEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_LOCATION_SERVICE_ENABLED, false)
    }

    override fun saveLocationServiceEnabled(isEnabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_LOCATION_SERVICE_ENABLED, isEnabled)
        }
    }
}