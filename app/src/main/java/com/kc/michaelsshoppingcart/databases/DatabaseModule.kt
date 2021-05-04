package com.kc.michaelsshoppingcart.databases

import android.content.Context
import androidx.room.Room
import com.kc.michaelsshoppingcart.constants.AppConstants
import com.kc.michaelsshoppingcart.daos.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideProductDao(productDatabase: ProductDatabase): ProductDao {
        return productDatabase.productDao()
    }

    @Provides
    @Singleton
    fun provideProductDatabase(@ApplicationContext appContext: Context): ProductDatabase {
        return Room.databaseBuilder(
            appContext,
            ProductDatabase::class.java,
            AppConstants.APP_DATABASE
        ).build()
    }
}