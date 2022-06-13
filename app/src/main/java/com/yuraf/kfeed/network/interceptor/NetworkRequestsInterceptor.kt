package com.yuraf.kfeed.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * @author Yura F (yura-f.github.io)
 */
internal class NetworkRequestsInterceptor : Interceptor {
    companion object {
        private const val HEADER_USER_AGENT = "User-Agent"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()

        builder.addHeader(HEADER_USER_AGENT, "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.61 Mobile Safari/537.36")

        return chain.proceed(builder.build())
    }
}