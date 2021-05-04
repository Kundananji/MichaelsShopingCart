package com.kc.michaelsshoppingcart.repositories

import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.kc.michaelsshoppingcart.BuildConfig
import com.kc.michaelsshoppingcart.R
import com.kc.michaelsshoppingcart.classes.Product
import com.kc.michaelsshoppingcart.constants.AppConstants
import com.kc.michaelsshoppingcart.constants.PayPalConstants
import com.kc.michaelsshoppingcart.daos.ProductDao
import com.kc.michaelsshoppingcart.network.apis.PayPalAccessTokenService
import com.kc.michaelsshoppingcart.network.apis.PayPalCheckOrderDetailsService
import com.kc.michaelsshoppingcart.network.apis.PayPalMakeOrderService
import com.kc.michaelsshoppingcart.network.requestBodies.PayPalAmount
import com.kc.michaelsshoppingcart.network.requestBodies.PayPalMakeOrderRequest
import com.kc.michaelsshoppingcart.network.requestBodies.PurchaseUnits
import com.kc.michaelsshoppingcart.network.responseBodies.MakeOrderResponseBody
import com.kc.michaelsshoppingcart.network.utils.GetTokenServiceGenerator
import com.kc.michaelsshoppingcart.network.utils.MakeOrderServiceGenerator
import com.kc.michaelsshoppingcart.utils.Logger
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class ProductRepository @Inject constructor(
    private val mProductDao: ProductDao,
    private val mSharedPreferences: SharedPreferences
){
    /**
     * Load all products currently stored in the database
     * if there are no products, insert the default ones
     */
    fun  getProducts(): LiveData<List<Product>> {
        val mAllProducts = mProductDao.getAllProducts()

            //no products, add default products
           val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val count = mProductDao.getCount()
                if(count ==0) {
                    val productIphone = Product(
                        id = 1,
                        name = "Iphone7",
                        numberInStock = 8,
                        description = "4.70-inch touchscreen display with a resolution of 750x1334 pixels at a pixel density of 326 pixels per inch (ppi) and an aspect ratio of 16:9",
                        price = 899.00,
                        picture = R.drawable.iphone,
                        category = "Electronics",
                        addedToCart = 0,
                            orderId = "0",
                            purchased = 0
                    )

                    val productSamSung = Product(
                            id = 2,
                            name = "Samsung Galaxy A 20",
                            numberInStock = 13,
                            description = "Colour: BlackCategoriesCellphones & Wearables / Cellphones / SmartphonesWarrantyFull (24 months)Basic ColoursBlackAudio Jacks3.5 mm mono (TS)Lens",
                            price = 605.00,
                            picture = R.drawable.samsung,
                            category = "Electronics",
                            addedToCart = 0,
                            orderId = "0",
                            purchased = 0
                    )
                    mProductDao.insert(productIphone)
                    mProductDao.insert(productSamSung)
                }
            }

       return mAllProducts
    }

    /**
     * Add product of interest to shoping cart
     * @param product : this is the product that has been selected
     */
    fun addToCart(product: Product) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val mProduct = mProductDao.loadProduct(product.id);
            if(mProduct.addedToCart == 0) {
                mProduct.addedToCart =1
            }
            else{
                mProduct.addedToCart =0
            }

            mProductDao.update(mProduct)
        }

    }

    /**
     * function to load products that have been added to the cart
     * @return LiveData Object containing list of products that have been checked out
     */
     fun getCheckedOutProducts(): LiveData<List<Product>> {
        return mProductDao.getCheckedOutProducts()
    }

    /**
     * function to check the order of an order that has been done
     * @return LiveData object with the MakeOrderResponse Body, containing the status of the order
     */
    fun checkOrderDetails() : LiveData<MakeOrderResponseBody> {
        val makeOrderResponse  = MutableLiveData<MakeOrderResponseBody>()
        val orderId = mSharedPreferences.getString(AppConstants.SHARED_PREFERENCES_ORDER_ID,null)
        if(orderId == null){
            makeOrderResponse.postValue(MakeOrderResponseBody(id="",status=PayPalConstants.STATUS_NO_ORDER,links= mutableListOf()))
            return makeOrderResponse
        }

        makeOrderResponse.postValue(MakeOrderResponseBody(id="",status=PayPalConstants.STATUS_PENDING,links= mutableListOf()))
        val accessToken: String? = mSharedPreferences.getString(AppConstants.SHARED_PREFERENCES_ACCESS_TOKEN,null)
        if(accessToken==null){
            Logger.writeLog(TAG, "Checking Order status", Logger.DEBUG)
            getAccessToken()
            makeOrderResponse.postValue(MakeOrderResponseBody(id="",status=PayPalConstants.STATUS_FAILED,links= mutableListOf()))
        }
        else{

            val checkOrderDetailsService: PayPalCheckOrderDetailsService =
                MakeOrderServiceGenerator.createService(
                    PayPalCheckOrderDetailsService::class.java,
                    accessToken
                )
            val call: Call<MakeOrderResponseBody?>? = checkOrderDetailsService.getOrderDetails(orderId)
            call!!.enqueue(object : Callback<MakeOrderResponseBody?> {

                override fun onResponse(
                    all: Call<MakeOrderResponseBody?>,
                    response: Response<MakeOrderResponseBody?>
                ) {
                    if (response.isSuccessful()) {

                        if (response.body() != null) {
                            val status = response.body()!!.status
                            val id = response.body()!!.id
                            val links = response.body()!!.links
                            Logger.writeLog(TAG, "Response Status is: ${status}", Logger.DEBUG)

                            makeOrderResponse.postValue(MakeOrderResponseBody(id=id,status=status,links= links))

                             if(status == PayPalConstants.STATUS_COMPLETED || status == PayPalConstants.STATUS_VOIDED || status == PayPalConstants.STATUS_APPROVED) {
                                 mSharedPreferences.edit()
                                     .putString(AppConstants.SHARED_PREFERENCES_ORDER_ID, null)
                                     .apply()

                                 //update local products to purchased for this particular order id; also reduce stock
                                 if(status == PayPalConstants.STATUS_COMPLETED ||status == PayPalConstants.STATUS_APPROVED ) {
                                     val scope = CoroutineScope(Dispatchers.IO)
                                     scope.launch {
                                         var prods: List<Product> = mProductDao.getProductsByOrderId(orderId)
                                         prods.forEach { thisProd ->
                                             thisProd.purchased = 1
                                             thisProd.numberInStock = (thisProd.numberInStock - 1)
                                             thisProd.addedToCart = 0
                                         }
                                         mProductDao.insert(prods)

                                     }
                                 }
                             }

                        } else {
                            Logger.writeLog(TAG, "Response is null ", Logger.DEBUG)
                            makeOrderResponse.postValue(MakeOrderResponseBody(id="",status=PayPalConstants.STATUS_FAILED,links= mutableListOf()))
                        }


                    } else {

                        Logger.writeLog(
                            TAG,
                            "No access to resource. ${response.message()}",
                            Logger.DEBUG
                        )
                        makeOrderResponse.postValue(MakeOrderResponseBody(id="",status=PayPalConstants.STATUS_FAILED,links= mutableListOf()))
                    }
                }

                override fun onFailure(call: Call<MakeOrderResponseBody?>, t: Throwable) {
                   Logger.writeLog(TAG, "No access to resource: ${t.message}", Logger.DEBUG)
                    makeOrderResponse.postValue(MakeOrderResponseBody(id="",status=PayPalConstants.STATUS_FAILED,links= mutableListOf()))
                }


            })
        }
        return makeOrderResponse
    }

    /**
     * function to create an order
     * @param products List of products to purchase
     * @return LiveData object containing MakeOrderResponseBody with the status of the created order
     */
    fun purchaseProducts(products: List<Product>) : LiveData<MakeOrderResponseBody> {

        val makeOrderResponse  = MutableLiveData<MakeOrderResponseBody>()
        makeOrderResponse.postValue(MakeOrderResponseBody(id="",status=PayPalConstants.STATUS_PENDING,links= mutableListOf()))

        val accessToken: String? = mSharedPreferences.getString(AppConstants.SHARED_PREFERENCES_ACCESS_TOKEN,null)
        if(accessToken==null){
            Logger.writeLog(TAG, "Refreshing  Credentials", Logger.DEBUG)
            makeOrderResponse.postValue(MakeOrderResponseBody(id="",status=PayPalConstants.STATUS_FAILED,links= mutableListOf()))
            getAccessToken()
       }
        else{

            var price : Double = 0.0
            products.forEach {
                price+= it.price
            }

            val purchaseUnitsList: MutableList<PurchaseUnits> = mutableListOf()
            val amount = PayPalAmount(PayPalConstants.CURRENCY_USD,price.toString())
            val purchaseUnit  = PurchaseUnits(amount)
            purchaseUnitsList.add(purchaseUnit)

            val makeOrderRequest =  PayPalMakeOrderRequest(PayPalConstants.INTENT_CAPTURE,purchaseUnitsList)

            val payPalMakeOrderService: PayPalMakeOrderService =
                MakeOrderServiceGenerator.createService(
                    PayPalMakeOrderService::class.java,
                   accessToken
                )
            val call: Call<MakeOrderResponseBody?>? = payPalMakeOrderService.makeOrder(makeOrderRequest)
            call!!.enqueue(object : Callback<MakeOrderResponseBody?> {

                override fun onResponse(
                    all: Call<MakeOrderResponseBody?>,
                    response: Response<MakeOrderResponseBody?>
                ) {
                    if (response.isSuccessful()) {

                        if (response.body() != null) {
                            val status = response.body()!!.status
                            val id = response.body()!!.id
                            Logger.writeLog(TAG, "Response Status is: ${status}", Logger.DEBUG)

                            //update products on local database

                            val scope = CoroutineScope(Dispatchers.IO)
                            scope.launch {

                                products.forEach { thisProd ->
                                    thisProd.purchased = 0
                                    thisProd.orderId = id
                                }
                                mProductDao.insert(products)
                            }

                            makeOrderResponse.postValue(response.body())
                            mSharedPreferences.edit().putString(AppConstants.SHARED_PREFERENCES_ORDER_ID,id).apply()

                        } else {
                            Logger.writeLog(TAG, "Response is null ", Logger.DEBUG)
                            makeOrderResponse.postValue(MakeOrderResponseBody(id="",status=PayPalConstants.STATUS_FAILED,links= mutableListOf()))
                        }


                    } else {
                        // error response, no access to resource?
                        Logger.writeLog(
                            TAG,
                            "No access to resource. ${response.message()}",
                            Logger.DEBUG
                        )
                        makeOrderResponse.postValue(MakeOrderResponseBody(id="",status=PayPalConstants.STATUS_FAILED,links= mutableListOf()))
                    }
                }

                override fun onFailure(call: Call<MakeOrderResponseBody?>, t: Throwable) {
                    Logger.writeLog(TAG, "No access to resource: ${t.message}", Logger.DEBUG)
                    makeOrderResponse.postValue(MakeOrderResponseBody(id="",status=PayPalConstants.STATUS_FAILED,links= mutableListOf()))
                }


            })
        }
      return makeOrderResponse
    }


    /**
     * function to fetch a fresh authorization token if one is not set or if it has expired
     * Authorization token is stored in shared preferences
     */

    fun getAccessToken(){

        val currentTimeSeconds: Long = TimeUnit.SECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS)
        val lastAccessed = mSharedPreferences.getLong(AppConstants.SHARED_PREFERENCES_TOKEN_ACCESS_TIME,0)
        val expiresIn  = mSharedPreferences.getLong(AppConstants.SHARED_PREFERENCES_TOKEN_EXPIRES_IN,0)
        val accessToken: String? = mSharedPreferences.getString(AppConstants.SHARED_PREFERENCES_ACCESS_TOKEN,null)

        Logger.writeLog(TAG, "Current Time: ${currentTimeSeconds}", Logger.DEBUG)
        Logger.writeLog(TAG, "Last Accessed: ${lastAccessed}", Logger.DEBUG)
        Logger.writeLog(TAG, "Expires: ${expiresIn}", Logger.DEBUG)
        Logger.writeLog(TAG, "accessToken: ${accessToken}", Logger.DEBUG)
        Logger.writeLog(TAG, "Time Difference: ${(currentTimeSeconds - lastAccessed )}", Logger.DEBUG)

        if(accessToken == null || (currentTimeSeconds - lastAccessed >= expiresIn )) {
            Logger.writeLog(TAG, "Getting Credentials", Logger.DEBUG)

            val grantType = "client_credentials"
            val payPalAccessToken: PayPalAccessTokenService =
                GetTokenServiceGenerator.createService(
                    PayPalAccessTokenService::class.java,
                    BuildConfig.CLIENT_ID,
                    BuildConfig.CLIENT_SECRET
                )
            val call: Call<ResponseBody?>? = payPalAccessToken.getAccessToken(grantType)
            call!!.enqueue(object : Callback<ResponseBody?> {

                override fun onResponse(
                    all: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    if (response.isSuccessful()) {
                        // user object available
                        if (response.body() != null) {
                            val responseString = response.body()!!.string()
                            Logger.writeLog(TAG, "Response String: ${responseString}", Logger.DEBUG)

                            val jElement: JsonElement = JsonParser().parse(responseString)
                            val jObject = jElement.asJsonObject

                            val accessTimeSeconds: Long =
                                TimeUnit.SECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS)

                            mSharedPreferences.edit()
                                .putString(
                                    AppConstants.SHARED_PREFERENCES_ACCESS_TOKEN, jObject.get(
                                        AppConstants.SHARED_PREFERENCES_ACCESS_TOKEN
                                    ).asString
                                )
                                .putString(
                                    AppConstants.SHARED_PREFERENCES_TOKEN_TYPE, jObject.get(
                                        AppConstants.SHARED_PREFERENCES_TOKEN_TYPE
                                    ).asString
                                )
                                .putString(
                                    AppConstants.SHARED_PREFERENCES_TOKEN_APP_ID, jObject.get(
                                        AppConstants.SHARED_PREFERENCES_TOKEN_APP_ID
                                    ).asString
                                )
                                .putLong(
                                    AppConstants.SHARED_PREFERENCES_TOKEN_EXPIRES_IN, jObject.get(
                                        AppConstants.SHARED_PREFERENCES_TOKEN_EXPIRES_IN
                                    ).asLong
                                )
                                .putLong(
                                    AppConstants.SHARED_PREFERENCES_TOKEN_ACCESS_TIME,
                                    accessTimeSeconds.toLong()
                                )
                                .putString(
                                    AppConstants.SHARED_PREFERENCES_TOKEN_SCOPE, jObject.get(
                                        AppConstants.SHARED_PREFERENCES_TOKEN_SCOPE
                                    ).asString
                                )
                                .apply()

                        } else {
                            Logger.writeLog(TAG, "Response is null ", Logger.DEBUG)
                        }


                    } else {
                        // error response, no access to resource?
                        Logger.writeLog(
                            TAG,
                            "No access to resource. ${response.message()}",
                            Logger.DEBUG
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    Logger.writeLog(TAG, "No access to resource: ${t.message}", Logger.DEBUG)
                }


            })
        }
        else{
            Logger.writeLog(TAG, "Getting are still valid, using cached credentials ", Logger.DEBUG)
            Logger.writeLog(TAG, "Token is: ${accessToken} ", Logger.DEBUG)
        }
    }
    companion object{
        private val TAG: String =  ProductRepository::class.java.simpleName;
    }

}