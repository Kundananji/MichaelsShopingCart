package com.kc.michaelsshoppingcart.network.utils

import android.text.TextUtils
import com.kc.michaelsshoppingcart.constants.AppConstants
import com.kc.michaelsshoppingcart.constants.PayPalConstants
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class GetTokenServiceGenerator {
    companion object {
        val API_BASE_URL = if(AppConstants.IS_LIVE) PayPalConstants.LIVE_URL  else PayPalConstants.SANDBOX_URL
        private val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()

        private val builder = Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())

        private var retrofit = builder.build()

        fun <S> createService(serviceClass: Class<S>?): S {
            return createService(serviceClass, null, null)
        }

        fun <S> createService(
            serviceClass: Class<S>?, username: String?, password: String?
        ): S {
            if (!TextUtils.isEmpty(username)
                && !TextUtils.isEmpty(password)
            ) {

                val authToken: String = Credentials.basic(username!!, password!!)
                return createService(serviceClass, authToken)
            }
            return createService(serviceClass, null)
        }

        fun <S> createService(
            serviceClass: Class<S>?, authToken: String?
        ): S {
            if (!TextUtils.isEmpty(authToken)) {

                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.level=(HttpLoggingInterceptor.Level.BODY)

                if (!httpClient.interceptors().contains(loggingInterceptor)) {
                    httpClient.addInterceptor(loggingInterceptor)
                    builder.client(httpClient.build())
                }

                val interceptor = AuthenticationInterceptor(authToken!!)
                if (!httpClient.interceptors().contains(interceptor)) {
                    httpClient.addInterceptor(interceptor)
                    builder.client(httpClient.build())
                    retrofit = builder.build()
                }
            }
            return retrofit.create(serviceClass)
        }
    }
}