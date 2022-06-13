package com.yuraf.kfeed.api

import com.yuraf.kfeed.BuildConfig
import com.yuraf.kfeed.api.data.SearchResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @author Yura F (yura-f.github.io)
 */
interface FlickrApi {
    @GET("rest")
    fun searchPhoto(@Query("lat") lat: String? = null,
               @Query("lon") lon: String? = null,
               @Query("api_key") apiKey: String = BuildConfig.API_KEY,
               @Query("method") method: String = "flickr.photos.search",
               @Query("radius") radius: String? = "0.1",
               @Query("extras") extras: String = "url_s, url_m, url_c, url_b",
               @Query("per_page") perPage: String = "1",
               @Query("accuracy") accuracy: String = "16",
               @Query("format") format: String = "json",
               @Query("nojsoncallback") nojsoncallback: String = "1"
    ): Observable<SearchResponse>
}