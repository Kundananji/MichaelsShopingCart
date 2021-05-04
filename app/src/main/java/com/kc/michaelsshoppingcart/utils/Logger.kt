package com.kc.michaelsshoppingcart.utils

import android.util.Log
import com.kc.michaelsshoppingcart.constants.AppConstants

class Logger {
    companion object{
        const val DEBUG = Log.DEBUG
        const val ERROR = Log.ERROR
        const val INFO = Log.INFO
        const val WARN = Log.WARN

        fun writeLog(TAG:String,message:String,type:Int){
            if(AppConstants.SHOW_LOGS) {
                if (type == DEBUG) Log.d(TAG, message)
                else if (type == ERROR) Log.e(TAG, message)
                else if (type == INFO) Log.i(TAG, message)
                else if (type == WARN) Log.w(TAG, message)
            }
        }
    }
}