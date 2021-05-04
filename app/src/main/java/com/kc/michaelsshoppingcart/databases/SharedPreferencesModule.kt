package com.kc.michaelsshoppingcart.databases

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.kc.michaelsshoppingcart.constants.AppConstants
import dagger.Module

import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class SharedPreferencesModule{

    @Provides
    fun provideSharedPreferences( application : Application): SharedPreferences {
        return application.getSharedPreferences(AppConstants.PRODUCT_SHARED_PREFERENCES,  Context.MODE_PRIVATE)
    }

}