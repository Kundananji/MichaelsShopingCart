package com.kc.michaelsshoppingcart.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kc.michaelsshoppingcart.R
import com.kc.michaelsshoppingcart.classes.Product
import com.kc.michaelsshoppingcart.constants.PayPalConstants
import com.kc.michaelsshoppingcart.network.responseBodies.PayPayLinkResponse
import com.kc.michaelsshoppingcart.recyclerAdapters.ProductListAdapter
import com.kc.michaelsshoppingcart.utils.Logger
import com.kc.michaelsshoppingcart.viewModels.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ShoppingCartActivity : AppCompatActivity() {
    private  val mProductViewModel: ProductViewModel by viewModels()
    private lateinit var  mProducts : List<Product>
    private lateinit  var recyclerView : RecyclerView
    private lateinit  var progressbar : ProgressBar
    private lateinit  var textview_no_products_found : TextView
    private lateinit  var button_checkout : Button
    private lateinit var image_failed : ImageView
    private lateinit var image_success: ImageView
    private lateinit var button_complete_payment : Button
    private var mLinks : List<PayPayLinkResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_cart)

         recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
         progressbar = findViewById<ProgressBar>(R.id.progressbar)
         textview_no_products_found = findViewById<TextView>(R.id.textview_no_products_found)
         button_checkout = findViewById<Button>(R.id.button_checkout)
         image_failed = findViewById(R.id.image_failed)
         image_success = findViewById(R.id.image_success)
        button_complete_payment = findViewById(R.id.button_complete_payment)


        GlobalScope.launch(Dispatchers.Main) {
            mProductViewModel.getAccessToken()
            val adapter =
                ProductListAdapter(this@ShoppingCartActivity) { product: Product, _: Int ->
                    Toast.makeText(this@ShoppingCartActivity, product.name, Toast.LENGTH_LONG)
                        .show()
                    GlobalScope.launch(Dispatchers.IO) {
                        mProductViewModel.addToCart(product)
                    }

                }
            recyclerView.adapter = adapter
            recyclerView.layoutManager =
                LinearLayoutManager(this@ShoppingCartActivity, RecyclerView.VERTICAL, false)

            mProductViewModel.getCheckedOutProducts().observe(this@ShoppingCartActivity) {
                // update UI
                mProducts = it
                adapter.setProducts(it)
                progressbar.visibility = View.GONE
                if (it.isEmpty()) {
                    textview_no_products_found.visibility = View.VISIBLE
                    button_checkout.visibility = View.GONE
                } else {
                    textview_no_products_found.visibility = View.GONE
                    button_checkout.visibility = View.VISIBLE
                }
                val msg = getString(R.string.textview_no_products_in_shopping_cart)
                textview_no_products_found.text = msg
            }

            button_complete_payment.setOnClickListener {
                if(mLinks!=null) {
                    mLinks!!.forEach { link ->
                        if (link.rel == PayPalConstants.REL_APPROVE && link.method == PayPalConstants.METHOD_GET) {
                            Toast.makeText(
                                this@ShoppingCartActivity,
                                R.string.toast_redirection_message,
                                Toast.LENGTH_LONG
                            ).show()
                            //redirect to payPal
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(link.href)
                            )
                            startActivity(browserIntent)

                        }
                    }
                }
                else{
                    Toast.makeText(this@ShoppingCartActivity,R.string.toast_message_action_not_available,Toast.LENGTH_LONG).show()
                }
            }

            button_checkout.setOnClickListener {
                if (mProducts.isEmpty()) {
                    Toast.makeText(
                        this@ShoppingCartActivity,
                        R.string.toast_message_no_products,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    button_checkout.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    textview_no_products_found.visibility = View.VISIBLE
                    progressbar.visibility = View.VISIBLE
                    //send request to create order and watch for response status
                    mProductViewModel.purchaseProducts(mProducts).observe(this@ShoppingCartActivity){
                        Logger.writeLog(TAG,"Order Status: ${ it.status}",Logger.DEBUG)
                        if(it.status == PayPalConstants.STATUS_FAILED){
                            //processing has failed due to some error
                           textview_no_products_found.text = getString(R.string.processing_payment_failed)
                           image_failed.visibility=View.VISIBLE
                            progressbar.visibility = View.GONE
                        }
                        else
                        if(it.status == PayPalConstants.STATUS_PENDING){
                            //Order is pending, show progress bar
                            textview_no_products_found.text = getString(R.string.processing_payment)
                        }
                        else
                        if(it.status == PayPalConstants.STATUS_CREATED){
                            //order has been created sucessfully, hide progress bar and show button to prompt user to
                            // navigate to PayPal to complete payment
                            image_success.visibility = View.VISIBLE
                            progressbar.visibility = View.GONE
                            textview_no_products_found.text = getString(R.string.payment_request_successful)
                            val links = it.links
                            links.forEach{ link ->

                                if(link.rel == PayPalConstants.REL_APPROVE && link.method == PayPalConstants.METHOD_GET){
                                    Toast.makeText(
                                        this@ShoppingCartActivity,
                                        R.string.toast_redirection_message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    //redirect to PayPal
                                    val browserIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(link.href)
                                    )
                                    startActivity(browserIntent)

                                }
                            }

                        }
                    }

                }
            }
        }
    }

    /**
     * Check order status when user returns to screen after completing checkout on paypal
     */
    override fun onRestart() {
        super.onRestart()
        checkOrderStatus()
    }

    /**
     * Check if there is a pending order upon loading this activity
     */
    override fun onResume() {
        super.onResume()
        checkOrderStatus()
    }

    /**
     * Check the status of an order if there is any and show the appropriate response
     */
    fun checkOrderStatus(){

        mProductViewModel.checkOrderStatus().observe(this@ShoppingCartActivity) {
            Logger.writeLog(TAG,"Order Status: ${ it.status}",Logger.DEBUG)
            mLinks = it.links
            if(it.status != PayPalConstants.STATUS_NO_ORDER){
                button_checkout.visibility = View.GONE
                recyclerView.visibility = View.GONE
                textview_no_products_found.visibility = View.VISIBLE
                progressbar.visibility = View.VISIBLE
                textview_no_products_found.text = getString(R.string.paypal_processing_payment_checking_order)
                image_failed.visibility = View.GONE
                image_success.visibility = View.GONE
                button_complete_payment.visibility = View.GONE

                if (it.status == PayPalConstants.STATUS_PENDING) {
                    textview_no_products_found.text = getString(R.string.paypal_processing_payment)
                }
                else
                    if (it.status == PayPalConstants.STATUS_CREATED) {
                        textview_no_products_found.text = getString(R.string.paypal_payment_created)
                        button_complete_payment.visibility = View.VISIBLE
                        image_success.visibility = View.VISIBLE
                        progressbar.visibility = View.GONE
                    }
                    else
                        if (it.status == PayPalConstants.STATUS_SAVED) {
                            image_success.visibility = View.VISIBLE
                            textview_no_products_found.text = getString(R.string.paypal_payment_saved)
                            button_complete_payment.visibility = View.VISIBLE
                            progressbar.visibility = View.GONE

                        }
                        else
                            if (it.status == PayPalConstants.STATUS_APPROVED) {
                                image_success.visibility = View.VISIBLE
                                progressbar.visibility = View.GONE
                                textview_no_products_found.text = getString(R.string.paypal_payment_approved)
                                progressbar.visibility = View.GONE
                            }
                            else
                                if (it.status == PayPalConstants.STATUS_VOIDED) {
                                    image_failed.visibility = View.VISIBLE
                                    progressbar.visibility = View.GONE
                                    textview_no_products_found.text = getString(R.string.paypal_payment_voided)
                                }
                                else
                                    if (it.status == PayPalConstants.STATUS_COMPLETED) {
                                        image_success.visibility = View.VISIBLE
                                        progressbar.visibility = View.GONE
                                        textview_no_products_found.text = getString(R.string.paypal_payment_completed)

                                    }
                                    else
                                        if (it.status == PayPalConstants.STATUS_PAYER_ACTION_REQUIRED) {
                                            textview_no_products_found.text = getString(R.string.paypal_payment_user_action_required)
                                            button_complete_payment.visibility = View.VISIBLE
                                            progressbar.visibility = View.GONE

                                        }
                                       else{
                                            image_failed.visibility = View.VISIBLE
                                            progressbar.visibility = View.GONE
                                            textview_no_products_found.text = getString(R.string.paypal_payment_error)
                                        }

            }

        }
    }


    companion object{
        val TAG = ShoppingCartActivity::class.java.simpleName
    }
}