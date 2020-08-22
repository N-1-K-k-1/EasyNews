package com.example.easynews.common

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.example.easynews.R
import com.google.android.gms.location.*
import java.util.*
private lateinit var fusedLocationClient: FusedLocationProviderClient
private lateinit var locationRequest: LocationRequest
private lateinit var locationCallback: LocationCallback



class GeoData(private val context: Context, private val savedInstanceState: Bundle?)  {

    private lateinit var geocode: Geocoder
    private var location: Location? = null

    fun getCityName(): String? {

        geocode = Geocoder(context, Locale.getDefault())

        val addresses = location?.latitude?.let { location?.longitude?.let { it1 ->
            geocode.getFromLocation(it,
                it1, 1)
        } }

        return addresses?.get(0)?.locality
    }

    fun getLanguage(): String {
        geocode = Geocoder(context, Locale.getDefault())
        val addresses = location?.latitude?.let { location?.longitude?.let { it1 ->
            geocode.getFromLocation(it,
                it1, 1)
        } }

        return if (addresses?.get(0)?.countryName == "Russia" || addresses?.get(0)?.countryName == "Россия" ||
            addresses?.get(0)?.countryName == "Russian Federation" || addresses?.get(0)?.countryName == "Российская Федерация" )
            "ru"
        else
            "en"
    }

    fun checkLocation(){
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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
                showAlertMessage()
            }
            else {
                getLocationUpdates()
                startLocationUpdates()
                stopLocationUpdates()
            }
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

    private fun showAlertMessage() {
        val dialog = AlertDialog.Builder(context)
        dialog.setMessage(context.getString(R.string.alert))
        dialog.setPositiveButton(context.getString(R.string.settings)) { _, _ ->
            val myIntent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
            ContextCompat.startActivity(context, myIntent, savedInstanceState)
        }
        dialog.setNegativeButton(context.getString(R.string.cancel)) { _, _ ->
            (context as Activity).finish()
        }
        dialog.setCancelable(false)
        dialog.show()
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
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}

