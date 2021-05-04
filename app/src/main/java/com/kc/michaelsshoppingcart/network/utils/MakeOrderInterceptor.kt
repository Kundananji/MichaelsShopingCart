package com.kc.michaelsshoppingcart.network.utils

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MakeOrderInterceptor(private val authToken: String) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var finalToken = "Bearer "+ authToken
        finalToken = finalToken.replace("\"","")
        val original: Request = chain.request()
        val builder: Request.Builder = original.newBuilder()
            .header("Authorization", finalToken)
            .header("Content-Type","application/json")
        val request: Request = builder.build()
        return chain.proceed(request)
    }
}