package com.yuraf.kfeed.api.data

import android.location.Location
import com.yuraf.kfeed.data.SearchPhoto
import com.yuraf.kfeed.data.SearchPhotos

/**
 * @author Yura F (yura-f.github.io)
 */
data class SearchResponse(val photos: SearchPhotosEntity?)

data class SearchPhotosEntity(val photo: List<SearchPhotoEntity>?)

data class SearchPhotoEntity(val id: String?,
                             val url_s: String?,
                             val url_m: String?,
                             val url_c: String?,
                             val url_b: String?)

fun SearchPhotosEntity.toData(localLocation: Location) = SearchPhotos(items = photo?.map { it.toData(localLocation) })

fun SearchPhotoEntity.toData(localLocation: Location) = SearchPhoto(
    id = id,
    url = url_b ?:url_c ?: url_m ?: url_s,
    localLocation = localLocation
)
