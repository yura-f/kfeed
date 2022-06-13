package com.yuraf.kfeed.logic

import android.location.Location
import com.yuraf.kfeed.data.SearchPhoto

/**
 * @author Yura F (yura-f.github.io)
 */
sealed class SearchState {
    object Loading : SearchState()
    data class ErrorLoading(val throwable: Throwable,
                            val photos: List<SearchPhoto>,
                            val isAddedNew: Boolean) : SearchState()

    data class SuccessLoad(val photos: List<SearchPhoto>,
                           val isAddedNew: Boolean) : SearchState()

    data class ErrorNotFoundPhoto(val location: Location) : SearchState()
}