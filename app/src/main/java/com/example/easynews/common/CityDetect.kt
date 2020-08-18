package com.example.easynews.common

import android.content.Context
import android.location.Geocoder
import android.util.Log
import java.util.*


internal class CityDetect(context: Context, private val latitude : Double, val longitude : Double)  {

    private val geocode = Geocoder(context, Locale.getDefault())

    init {
        val addresses = geocode.getFromLocation(latitude, longitude, 1)
        city = addresses[0].locality

        language = if (addresses[0].countryName == "Russia")
            "ru"
        else
            "en"

        Log.d("Init", "Init")
    }

    fun getCityName(): String {
        val addresses = geocode.getFromLocation(latitude, longitude, 1)

        return addresses[0].locality
    }

    companion object {
        @JvmStatic var city = ""
        @JvmStatic var language = ""
    }
}