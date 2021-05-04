package com.kc.michaelsshoppingcart.network.apis

import com.kc.michaelsshoppingcart.constants.PayPalConstants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface PayPalAccessTokenService {
    @FormUrlEncoded
    @POST(PayPalConstants.ACCESS_TOKEN_PATH)
    fun getAccessToken(@Field("grant_type") grantType:String ): Call<ResponseBody?>?
}