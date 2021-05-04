package com.kc.michaelsshoppingcart

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kc.michaelsshoppingcart.classes.Product
import com.kc.michaelsshoppingcart.recyclerAdapters.ProductListAdapter
import com.kc.michaelsshoppingcart.ui.ShoppingCartActivity
import com.kc.michaelsshoppingcart.viewModels.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private  val mProductViewModel: ProductViewModel by viewModels()
    var textCartItemCount: TextView? = null
    var mCartItemCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener {
            startActivity(Intent(this@MainActivity, ShoppingCartActivity::class.java))
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val progressbar = findViewById<ProgressBar>(R.id.progressbar)
        val textview_no_products_found = findViewById<TextView>(R.id.textview_no_products_found)
        GlobalScope.launch(Dispatchers.Main) {
            mProductViewModel.getAccessToken()
            val adapter = ProductListAdapter(this@MainActivity) { product: Product, _: Int ->
                System.out.println(" Updating product: ${product.id}")
                GlobalScope.launch(Dispatchers.IO) {
                    mProductViewModel.addToCart(product)
                }

            }
            recyclerView.adapter = adapter
            recyclerView.layoutManager =
                LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)

            //get checkout items and update menu
            mProductViewModel.getCheckedOutProducts().observe(this@MainActivity){
                mCartItemCount = it.size
                invalidateOptionsMenu()
            }

            //get all products and update ui
            mProductViewModel.getProducts().observe(this@MainActivity) {
                adapter.setProducts(it)
                progressbar.visibility = View.GONE

                if (it.isEmpty()) {
                    textview_no_products_found.visibility = View.VISIBLE
                } else {
                    textview_no_products_found.visibility = View.GONE
                }
                val msg = getString(R.string.textview_no_products_found)
                textview_no_products_found.text = msg
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        val menuItem = menu!!.findItem(R.id.action_checkout)
        val actionView = menuItem.actionView
        textCartItemCount = actionView.findViewById(R.id.cart_badge) as TextView
        setupBadge()
        actionView.setOnClickListener {

                onOptionsItemSelected(menuItem)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_checkout){
            startActivity(Intent(this@MainActivity, ShoppingCartActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupBadge() {

        if (textCartItemCount != null) {
            if (mCartItemCount == 0) {
                if (textCartItemCount!!.getVisibility() != View.GONE) {
                    textCartItemCount!!.setVisibility(View.GONE)
                }
            } else {
                textCartItemCount!!.setText((Math.min(mCartItemCount, 99)).toString())
                if (textCartItemCount!!.getVisibility() != View.VISIBLE) {
                    textCartItemCount!!.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}