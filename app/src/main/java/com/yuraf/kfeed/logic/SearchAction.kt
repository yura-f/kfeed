package com.yuraf.kfeed.logic

import android.location.Location

/**
 * @author Yura F (yura-f.github.io)
 */
sealed class SearchAction {
    data class LoadPhoto(val location: Location) : SearchAction()
    object GetSavedPhotos : SearchAction()
}