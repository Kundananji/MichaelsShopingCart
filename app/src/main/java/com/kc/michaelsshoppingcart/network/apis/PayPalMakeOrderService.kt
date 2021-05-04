package com.kc.michaelsshoppingcart.network.apis

import com.kc.michaelsshoppingcart.constants.PayPalConstants
import com.kc.michaelsshoppingcart.network.requestBodies.PayPalMakeOrderRequest
import com.kc.michaelsshoppingcart.network.responseBodies.MakeOrderResponseBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface PayPalMakeOrderService {
    @POST(PayPalConstants.ORDERS_PATH)
    fun makeOrder(@Body makeOrderRequest: PayPalMakeOrderRequest ): Call<MakeOrderResponseBody?>?
}