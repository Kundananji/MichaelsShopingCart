package com.kc.michaelsshoppingcart.network.responseBodies

data class MakeOrderResponseBody (
    val id: String,
    val status: String,
    val links: List<PayPayLinkResponse>
    )