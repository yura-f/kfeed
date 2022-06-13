package com.yuraf.kfeed.data

import android.location.Location
import com.yuraf.kfeed.common.UniqueId
import com.yuraf.kfeed.utils.toText

/**
 * @author Yura F (yura-f.github.io)
 */
data class SearchPhoto(val id: String? = null,
                       val url: String? = null,
                       val localLocation: Location?,
                       override val uniqueId: String? = id?.plus(localLocation?.toText())
): UniqueId {
    fun isCompleted() : Boolean {
        return id != null
    }
}
