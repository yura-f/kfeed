package com.yuraf.kfeed.utils

import android.location.Location

/**
 * @author Yura F (yura-f.github.io)
 */
fun Location.toText(): String {
    return "($latitude, $longitude)"
}