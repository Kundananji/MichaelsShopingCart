package com.kc.michaelsshoppingcart.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kc.michaelsshoppingcart.classes.Product
import com.kc.michaelsshoppingcart.daos.ProductDao

@Database(entities =[Product::class],version = 1, exportSchema = false)
abstract class ProductDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}