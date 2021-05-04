package com.kc.michaelsshoppingcart.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName="product")
@Parcelize
data class Product(
        @PrimaryKey
        @ColumnInfo(name="product_id") var id: Int,
        @ColumnInfo(name="name") var name: String,
        @ColumnInfo(name="number_in_stock") var numberInStock: Int,
        @ColumnInfo(name="description") var description: String,
        @ColumnInfo(name="price") var price: Double,
        @ColumnInfo(name="picture") var picture : Int,
        @ColumnInfo(name="category") var category: String,
        @ColumnInfo(name="added_to_cart") var addedToCart: Int,
        @ColumnInfo(name="order_id") var orderId: String,
        @ColumnInfo(name="purchased") var purchased: Int
): Parcelable