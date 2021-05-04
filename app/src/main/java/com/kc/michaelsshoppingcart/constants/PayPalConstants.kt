package com.kc.michaelsshoppingcart.constants

class PayPalConstants {
    companion object{
         const val STATUS_NO_ORDER = "NO_ORDER"
         const val STATUS_APPROVED = "APPROVED"
         const val STATUS_VOIDED = "VOIDED"
         const val STATUS_COMPLETED = "COMPLETED"
         const val STATUS_PAYER_ACTION_REQUIRED = "PAYER_ACTION_REQUIRED"
         const val STATUS_FAILED = "FAILED"
         const val STATUS_CREATED = "CREATED"
         const val STATUS_SAVED = "SAVED"
         const val STATUS_PENDING = "PENDING"

         const val METHOD_GET = "GET"
         const val REL_SELF = "self"
         const val REL_APPROVE = "approve"

         const val SANDBOX_URL = "https://api-m.sandbox.paypal.com"
         const val LIVE_URL = "https://api-m.paypal.com"

         const val ORDERS_PATH = "/v2/checkout/orders"
         const val ACCESS_TOKEN_PATH = "/v1/oauth2/token"
         const val INTENT_CAPTURE = "CAPTURE"

         const val CURRENCY_USD = "USD"

    }
}