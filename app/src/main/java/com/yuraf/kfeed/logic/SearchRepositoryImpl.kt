package com.yuraf.kfeed.logic

import android.location.Location
import com.yuraf.kfeed.api.FlickrApi
import com.yuraf.kfeed.api.data.toData
import com.yuraf.kfeed.data.SearchPhoto
import com.yuraf.kfeed.utils.ioScheduler
import io.reactivex.rxjava3.core.Observable

/**
 * @author Yura F (yura-f.github.io)
 */
class SearchRepositoryImpl(val flickrApi: FlickrApi) : SearchRepository {
    private val allPhotosMap: LinkedHashMap<Location, SearchPhoto> = linkedMapOf()

    override fun searchPhoto(actions: Observable<SearchAction>): Observable<SearchState> {
        return actions.flatMap { action ->
            when (action) {
                is SearchAction.LoadPhoto -> {
                    val location = action.location
                    ioScheduler(flickrApi.searchPhoto(location.latitude.toString(), location.longitude.toString()))
                        .map { searchResponse ->
                            val photo = searchResponse.photos?.toData(location)?.items?.firstOrNull()
                            var isAddedNew = false

                            photo?.let { newPhoto ->
                                //we've got photo data and
                                //try to find our photo by location
                                getSearchPhoto(location)?.let {
                                    //update old photo
                                    allPhotosMap.put(location, newPhoto)
                                } ?: run {
                                    //add new photo on top
                                    isAddedNew = true
                                    addNewPhotoOnTop(location = location, searchPhoto = newPhoto)
                                }
                            } ?: run {
                                //we haven't got photo data
                                //try to find our photo and remove it
                                getSearchPhoto(location)?.let { photoByLocation ->
                                    if (!photoByLocation.isCompleted()) {
                                        allPhotosMap.remove(location)
                                    }
                                } ?: run {
                                    //to return error not found photo
                                    return@map SearchState.ErrorNotFoundPhoto(location)
                                }
                            }

                            return@map SearchState.SuccessLoad(getAllPhotos(), isAddedNew)
                        }
                        .onErrorReturn {
                            var isAddedNew = false
                            //we haven't added this photo yet
                            if(getSearchPhoto(location) == null) {
                                isAddedNew = true
                                addNewPhotoOnTop(location = location, searchPhoto = SearchPhoto(localLocation = location))
                            }
                            SearchState.ErrorLoading(throwable = it, photos = getAllPhotos(), isAddedNew = isAddedNew)
                        }
                        .startWithItem(SearchState.Loading)
                }

                SearchAction.GetSavedPhotos -> {
                    Observable.just(SearchState.SuccessLoad(getAllPhotos(), true))
                }
            }
        }
    }

    private fun addNewPhotoOnTop(location: Location, searchPhoto: SearchPhoto) {
        allPhotosMap.remove(location)
        val newMap = allPhotosMap.clone() as LinkedHashMap<Location, SearchPhoto>
        allPhotosMap.clear()
        allPhotosMap[location] = searchPhoto
        allPhotosMap.putAll(newMap)
    }

    private fun getSearchPhoto(location : Location) : SearchPhoto? = allPhotosMap[location]

    private fun getAllPhotos() : List<SearchPhoto> {
        return allPhotosMap.values.toList()
    }
}