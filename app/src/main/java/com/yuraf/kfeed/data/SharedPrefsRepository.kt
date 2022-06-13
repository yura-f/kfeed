package com.yuraf.kfeed.data

/**
 * @author Yura F (yura-f.github.io)
 */
interface SharedPrefsRepository {
    fun isLocationServiceEnabled(): Boolean

    fun saveLocationServiceEnabled(isEnabled: Boolean)
}