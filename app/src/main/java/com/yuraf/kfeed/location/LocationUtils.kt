package com.yuraf.kfeed.location

import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.provider.Settings

/**
 * @author Yura F (yura-f.github.io)
 */
class LocationUtils {
    companion object {
        fun isLocationEnabled(context: Context) : Boolean {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // This is new method provided in API 28
                    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    lm.isLocationEnabled
                } else {
                    // This is Deprecated in API 28
                    val mode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF)
                    mode != Settings.Secure.LOCATION_MODE_OFF
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}