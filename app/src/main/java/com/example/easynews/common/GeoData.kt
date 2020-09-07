package com.example.easynews.common

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import com.google.android.gms.location.*
import java.util.*


private lateinit var fusedLocationClient: FusedLocationProviderClient
private lateinit var locationRequest: LocationRequest
private lateinit var locationCallback: LocationCallback



class GeoData(private val context: Context)  {

    private lateinit var geocode: Geocoder
    private var location: Location? = null

    fun checkLocation(){
        if (!checkPermissions()) {
            requestPermissions (
                context as Activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET
                ), 1)
        }
        else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            if (getLastKnownLocation() == null) {
                getLocationUpdates()
                startLocationUpdates()
                //Thread.sleep(100)
                //stopLocationUpdates()
                Log.e("Last location", "Null")
            }
        }
    }

    fun getCityName(): String? {

        geocode = Geocoder(context, Locale.getDefault())

        val addresses = location?.latitude?.let { location?.longitude?.let { it1 ->
            geocode.getFromLocation(it,
                    it1, 1)
        } }

        return addresses?.get(0)?.locality
    }

    fun getLanguage(): String {
        geocode = Geocoder(context, Locale.ENGLISH)
        val addresses = location?.latitude?.let { location?.longitude?.let { it1 ->
            geocode.getFromLocation(it,
                    it1, 1)
        } }

        return if (addresses?.get(0)?.countryName == "Russia" || addresses?.get(0)?.countryName == "Russian Federation")
            "ru"
        else
            "en"
    }

    fun getCountry(): String {
        if (checkPermissions()) {
            geocode = Geocoder(context, Locale.ENGLISH)
            val addresses = location?.latitude?.let { location?.longitude?.let { it1 ->
                geocode.getFromLocation(it,
                    it1, 1)
            } }
                when(addresses?.get(0)?.countryName) {
                    "Russia" -> { return "ru" }
                    "Russian Federation" -> { return "ru" }
                    "United Arab Emirates" -> { return "ae" }
                    "UAE" -> { return "ae" }
                    "Argentina" -> { return "ar" }
                    "Austria" -> { return "at" }
                    "Australia" -> { return "au" }
                    "Belgium" -> { return "be" }
                    "Bulgaria" -> { return "bg" }
                    "Brazil" -> { return "br" }
                    "Canada" -> { return "ca" }
                    "Switzerland" -> { return "ch" }
                    "China" -> { return "cn" }
                    "Colombia" -> { return "co" }
                    "Cuba" -> { return "cu" }
                    "Czech Republic" -> { return "cz" }
                    "Germany" -> { return "de" }
                    "Egypt" -> { return "eg" }
                    "France" -> { return "fr" }
                    "United Kingdom" -> { return "gb" }
                    "Greece" -> { return "gr" }
                    "Hong Kong" -> { return "hk" }
                    "Hungary" -> { return "hu" }
                    "Indonesia" -> { return "id" }
                    else -> { return "gb" }
                }
        }
        else {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            println(tm.networkCountryIso)

            return tm.networkCountryIso
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers: List<String> = locationManager.getProviders(true)

        for (i in providers.size - 1 downTo 0) {
            location = locationManager.getLastKnownLocation(providers[i])
            if (location != null)
                break
        }

        val gps = DoubleArray(2)

        if (location != null) {
            gps[0] = location?.latitude!!
            gps[1] = location?.longitude!!
            Log.e("gpsLat", gps[0].toString())
            Log.e("gpsLong", gps[1].toString())
        }

        return location
    }

    fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = 50000
        locationRequest.fastestInterval = 50000
        locationRequest.smallestDisplacement = 170f //170 m = 0.1 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //according to your app
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                if (locationResult.locations.isNotEmpty()) {
                    location = locationResult.lastLocation
                }
            }
        }
    }

    // Start location updates
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null /* Looper */
        )
    }

    // Stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}

