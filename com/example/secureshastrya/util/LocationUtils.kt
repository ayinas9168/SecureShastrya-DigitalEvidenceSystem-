package com.example.secureshastrya.util

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices

object LocationUtils {

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context, callback: (String) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    callback("${location.latitude},${location.longitude}")
                } else {
                    callback("0,0")
                }
            }
            .addOnFailureListener {
                callback("0,0")
            }
    }
}