package com.kc.michaelsshoppingcart.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.kc.michaelsshoppingcart.classes.Product
import com.kc.michaelsshoppingcart.network.responseBodies.MakeOrderResponseBody
import com.kc.michaelsshoppingcart.repositories.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel(){
    fun addToCart(product: Product) {
       productRepository.addToCart(product)
    }

      fun getProducts() : LiveData<List<Product>> {
        return productRepository.getProducts()
    }

     fun getCheckedOutProducts() : LiveData<List<Product>> {
        return productRepository.getCheckedOutProducts()
    }

    fun purchaseProducts(products: List<Product>) : LiveData<MakeOrderResponseBody>{
        return productRepository.purchaseProducts(products)
    }

    fun getAccessToken() {
        return productRepository.getAccessToken()
    }

    fun checkOrderStatus(): LiveData<MakeOrderResponseBody> {
        return productRepository.checkOrderDetails()
    }

}