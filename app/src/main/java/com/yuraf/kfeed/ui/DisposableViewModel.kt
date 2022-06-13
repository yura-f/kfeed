package com.yuraf.kfeed.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 * @author Yura F (yura-f.github.io)
 */
open class DisposableViewModel<T> : ViewModel() {
    protected val compositeDisposable = CompositeDisposable()
    protected val actions: PublishSubject<T> = PublishSubject.create()
    val error = MutableLiveData<Int>()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}