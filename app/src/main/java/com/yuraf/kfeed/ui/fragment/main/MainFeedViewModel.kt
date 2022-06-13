package com.yuraf.kfeed.ui.fragment.main

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.yuraf.kfeed.R
import com.yuraf.kfeed.data.SearchPhoto
import com.yuraf.kfeed.logic.SearchAction
import com.yuraf.kfeed.logic.SearchRepository
import com.yuraf.kfeed.logic.SearchState
import com.yuraf.kfeed.ui.DisposableViewModel
import com.yuraf.kfeed.utils.schedulers
import com.yuraf.kfeed.utils.toText
import timber.log.Timber

class MainFeedViewModel(val searchRepository: SearchRepository) : DisposableViewModel<SearchAction>() {
    val itemsInfo = MutableLiveData<SearchPhotoItemsInfo>()
    val loading = MutableLiveData<Boolean>()

    init {
        compositeDisposable.add(schedulers(searchRepository.searchPhoto(actions))
            .subscribe {
                when (it) {
                    SearchState.Loading -> {
                        Timber.d("Loading")
                        loading.value = true
                    }

                    is SearchState.SuccessLoad -> {
                        Timber.d("SuccessLoad")
                        loading.value = false
                        itemsInfo.value = SearchPhotoItemsInfo(it.photos, it.isAddedNew)
                    }

                    is SearchState.ErrorLoading -> {
                        loading.value = false
                        Timber.e("ErrorLoading: ${it.throwable}" )
                        itemsInfo.value = SearchPhotoItemsInfo(it.photos, it.isAddedNew)
                    }

                    is SearchState.ErrorNotFoundPhoto -> {
                        loading.value = false
                        Timber.e("ErrorNotFoundPhoto: ${it.location.toText()}" )
                        error.value = R.string.not_found_photo
                    }
                }
            })
    }

    fun loadImage(location: Location) {
        actions.onNext(SearchAction.LoadPhoto(location))
    }

    fun checkPhotos() {
        actions.onNext(SearchAction.GetSavedPhotos)
    }

    data class SearchPhotoItemsInfo(val items: List<SearchPhoto>, val isAddedNew: Boolean)
}