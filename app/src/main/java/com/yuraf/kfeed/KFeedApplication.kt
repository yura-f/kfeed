package com.yuraf.kfeed

import android.app.Application
import com.yuraf.kfeed.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

/**
 * @author Yura F (yura-f.github.io)
 */
class KFeedApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@KFeedApplication)
            modules(listOf(appModule))
        }
    }
}