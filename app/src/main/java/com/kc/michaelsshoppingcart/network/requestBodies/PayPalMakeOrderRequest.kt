package com.kc.michaelsshoppingcart.network.requestBodies

data class PayPalMakeOrderRequest (
    val intent : String ,
    val purchase_units: List<PurchaseUnits>
)