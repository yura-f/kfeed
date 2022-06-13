package com.yuraf.kfeed.di

import android.content.Context
import coil.ImageLoader
import com.yuraf.kfeed.BuildConfig
import com.yuraf.kfeed.api.FlickrApi
import com.yuraf.kfeed.data.SharedPrefsRepository
import com.yuraf.kfeed.data.SharedPrefsRepositoryImpl
import com.yuraf.kfeed.logic.SearchRepository
import com.yuraf.kfeed.logic.SearchRepositoryImpl
import com.yuraf.kfeed.network.interceptor.NetworkRequestsInterceptor
import com.yuraf.kfeed.ui.fragment.main.MainFeedViewModel
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @author Yura F (yura-f.github.io)
 */
val appModule = module {
    single { androidContext().getSharedPreferences("kfeed", Context.MODE_PRIVATE) }
    single<SharedPrefsRepository> { SharedPrefsRepositoryImpl(sharedPreferences = get()) }
    single<SearchRepository> { SearchRepositoryImpl(flickrApi = get()) }
    single { provideImageLoader(context = androidContext(), okHttpClient = get()) }

    single { provideRetrofit(get()) }
    factory { provideOkHttpClient() }
    factory { provideFlickrApi(get()) }

    viewModel { MainFeedViewModel(searchRepository = get()) }
}

fun provideOkHttpClient() = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(NetworkRequestsInterceptor())
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        })
        .build()

fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()
}

fun provideFlickrApi(retrofit: Retrofit): FlickrApi = retrofit.create(FlickrApi::class.java)

fun provideImageLoader(context: Context, okHttpClient: OkHttpClient): ImageLoader = ImageLoader.Builder(context)
    .okHttpClient(okHttpClient)
    .build()