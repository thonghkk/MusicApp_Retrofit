package com.example.retrofit_musicapp.utils

import android.app.Service
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class CheckNetwork {
    companion object {
        lateinit var context: Context
        var connectivity: ConnectivityManager? = null
        var info: NetworkInfo? = null
        fun getContextThis(context: Context) {
            Companion.context = context
        }

        fun checkNetwork(): Boolean {
            //check Internet
            connectivity =
                context.getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
            var check = true
            if (connectivity != null) {
                info = connectivity!!.activeNetworkInfo
                if (info != null) {
                    if (info!!.state == NetworkInfo.State.CONNECTED) {
                        check = true
                    }
                } else {
                    check = false
                }
            }
            return check
        }
    }
}