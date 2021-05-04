package com.kc.michaelsshoppingcart.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kc.michaelsshoppingcart.classes.Product


@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(products: List<Product>)

    @Query("SELECT * FROM product ORDER BY name ASC")
    fun getAllProducts(): LiveData<List<Product>>

    @Query("SELECT * FROM product WHERE product_id = :productId")
    fun loadProduct(productId: Int): Product

    @Update(onConflict = OnConflictStrategy.REPLACE)
     fun update(product: Product)

     @Query("SELECT * FROM product WHERE added_to_cart = 1 ORDER BY name ASC")
     fun getCheckedOutProducts(): LiveData<List<Product>>

    @Query("SELECT * FROM product WHERE order_id = :orderId AND purchased = 0 ORDER BY name ASC")
    fun getProductsByOrderId(orderId: String): List<Product>

    @Query("SELECT COUNT(product_id) FROM product ")
    fun getCount(): Int
}