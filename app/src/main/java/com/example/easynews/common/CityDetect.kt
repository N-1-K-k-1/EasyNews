package com.example.easynews.common

import android.content.Context
import android.location.Geocoder
import java.util.*


internal class CityDetect(context: Context)  {

    private val geocode = Geocoder(context, Locale.getDefault())

    fun locateCity (latitude: Double, longitude: Double): String {
        val addresses = geocode.getFromLocation(latitude, longitude, 1)

        return addresses[0].locality
    }
}