package com.yuraf.kfeed.logic

import io.reactivex.rxjava3.core.Observable

/**
 * @author Yura F (yura-f.github.io)
 */
interface SearchRepository {
    fun searchPhoto(actions: Observable<SearchAction>): Observable<SearchState>
}