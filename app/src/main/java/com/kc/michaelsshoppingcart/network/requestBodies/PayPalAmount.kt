package com.kc.michaelsshoppingcart.network.requestBodies

data class PayPalAmount (
    val currency_code : String,
    val value: String
   )