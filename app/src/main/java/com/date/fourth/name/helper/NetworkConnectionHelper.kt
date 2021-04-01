package com.sasco.user.helper

import android.content.Context
import android.net.ConnectivityManager


class NetworkConnectionHelper {
    companion object {
        fun isNetworkConnected(context: Context): Boolean {
            val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo

            return netInfo != null && netInfo.isConnectedOrConnecting
        }
    }
}