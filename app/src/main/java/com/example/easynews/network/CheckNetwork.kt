package com.example.easynews.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.os.Build
import android.util.Log

class CheckNetwork(private val context : Context) {
    @SuppressLint("MissingPermission", "LongLogTag")
    fun registerDefaultNetworkCallback() {
        try {
            val connectivityManager =
                (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(object : NetworkCallback() {
                    override fun onAvailable(network : Network) {
                        super.onAvailable(network)
                        GlobalVars.isNetworkConnected = true
                        Log.d("FLABS:", "onAvailable")
                    }

                    override fun onLost(network : Network) {
                        super.onLost(network)
                        GlobalVars.isNetworkConnected = false
                        Log.d("FLABS:", "onLost")
                    }

                    override fun onBlockedStatusChanged(
                        network : Network,
                        blocked : Boolean
                    ) {
                        super.onBlockedStatusChanged(network, blocked)
                        Log.d("FLABS:", "onBlockedStatusChanged")
                    }

                    override fun onCapabilitiesChanged(
                        network : Network,
                        networkCapabilities : NetworkCapabilities
                    ) {
                        super.onCapabilitiesChanged(network, networkCapabilities)
                        Log.d("FLABS:", "onCapabilitiesChanged")
                    }

                    override fun onLinkPropertiesChanged(
                        network : Network,
                        linkProperties : LinkProperties
                    ) {
                        super.onLinkPropertiesChanged(network, linkProperties)
                        Log.d("FLABS:", "onLinkPropertiesChanged")
                    }

                    override fun onLosing(network : Network, maxMsToLive : Int) {
                        super.onLosing(network, maxMsToLive)
                        Log.d("FLABS:", "onLosing")
                    }

                    override fun onUnavailable() {
                        super.onUnavailable()
                        Log.d("FLABS:", "onUnavailable")
                    }
                })
            }
        } catch (e : Exception) {
            Log.d("FLABS: Exception in registerDefaultNetworkCallback", "hello")
            GlobalVars.isNetworkConnected = false
        }
    }

    @SuppressLint("MissingPermission", "LongLogTag")
    fun registerNetworkCallback() {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val builder = NetworkRequest.Builder()

            connectivityManager.registerNetworkCallback(
                builder.build(),
                object : NetworkCallback() {
                    override fun onAvailable(network : Network) {
                        super.onAvailable(network)
                        GlobalVars.isNetworkConnected = true
                        Log.d("FLABS:", "onAvailable")
                    }

                    override fun onLost(network : Network) {
                        super.onLost(network)
                        GlobalVars.isNetworkConnected = false
                        Log.d("FLABS:", "onLost")
                    }

                    override fun onBlockedStatusChanged(
                        network : Network,
                        blocked : Boolean
                    ) {
                        super.onBlockedStatusChanged(network, blocked)
                        Log.d("FLABS:", "onBlockedStatusChanged")
                    }

                    override fun onCapabilitiesChanged(
                        network : Network,
                        networkCapabilities : NetworkCapabilities
                    ) {
                        super.onCapabilitiesChanged(network, networkCapabilities)
                        Log.d("FLABS:", "onCapabilitiesChanged")
                    }

                    override fun onLinkPropertiesChanged(
                        network : Network,
                        linkProperties : LinkProperties
                    ) {
                        super.onLinkPropertiesChanged(network, linkProperties)
                        Log.d("FLABS:", "onLinkPropertiesChanged")
                    }

                    override fun onLosing(
                        network : Network,
                        maxMsToLive : Int
                    ) {
                        super.onLosing(network, maxMsToLive)
                        Log.d("FLABS:", "onLosing")
                    }

                    override fun onUnavailable() {
                        super.onUnavailable()
                        Log.d("FLABS:", "onUnavailable")
                    }
                })
        } catch (e : Exception) {
            Log.d("FLABS: Exception in registerNetworkCallback", "hello")
            Log.e("Callback Exception", e.toString())
            GlobalVars.isNetworkConnected = false
        }
    }

}