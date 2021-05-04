package com.kc.michaelsshoppingcart.network.utils

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AuthenticationInterceptor(private val authToken: String) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val builder: Request.Builder = original.newBuilder()
            .header("Authorization", authToken)
            .header("Content-Type","application/x-www-form-urlencoded")
        val request: Request = builder.build()
        return chain.proceed(request)
    }
}