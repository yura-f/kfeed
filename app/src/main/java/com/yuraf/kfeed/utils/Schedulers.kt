package com.yuraf.kfeed.utils

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * @author Yura F (yura-f.github.io)
 */
fun <T : Any> schedulers(observable: Observable<T>): Observable<T> = observable
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())

fun <T : Any> ioScheduler(observable: Observable<T>): Observable<T> = observable
    .subscribeOn(Schedulers.io())