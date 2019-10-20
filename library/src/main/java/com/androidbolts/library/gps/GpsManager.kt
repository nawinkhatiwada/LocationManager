package com.androidbolts.library.gps

import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.androidbolts.library.GPSLocationManager
import com.androidbolts.library.utils.LocationConstants
import com.androidbolts.library.utils.LocationConstants.REQUEST_CHECK_SETTINGS
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import java.text.DateFormat
import java.util.*

class GpsManager private constructor() : GpsProvider() {
    //TODO sav location ko kaam yaha
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var mSettingsClient: SettingsClient
    private lateinit var mLocationRequest: LocationRequest
    private var mLocationCallback: LocationCallback? = null
    private var mCurrentLocation: Location? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null


    companion object {
        private var gpsManager: GpsManager? = null
        fun getGpsManager(): GpsManager {
            if (gpsManager == null) {
                gpsManager = GpsManager()
            }
            return gpsManager!!
        }
    }

    override fun onResume() {
        Log.d("Tag", "onResume called")
    }

    override fun onPause() {
        stopLocationUpdates()
    }

    override fun onDestroy() {
        startLocationUpdates()
    }

    override fun get(hasTimeOut: Boolean) {
        getLocation(hasTimeOut)
    }

    private fun getLocation(hasTimeOut: Boolean) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext())
        mSettingsClient = LocationServices.getSettingsClient(getContext())
        createLocationRequest()
        createLocationCallback()
        buildLocationSettingsRequest()
        startLocationUpdates()
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = LocationConstants.UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.fastestInterval =
            LocationConstants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                mCurrentLocation = locationResult?.lastLocation

                if (getLocationListener() != null) {
                    getLocationListener()?.onLocationChanged(mCurrentLocation)
                } else {
                    throw Exception("LocationListener is null. add setListener(this) on builder function of Location Manager.")
                }
            }
        }
    }

    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        mLocationSettingsRequest = builder.build()
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)?.addOnSuccessListener {
            //remove if the task is already running
            stopLocationUpdates()
            mFusedLocationClient?.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback, Looper.myLooper()
            )
        }?.addOnFailureListener {
            when ((it as ApiException).statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i(
                        "LocationManager",
                        "Location settings are not satisfied. Attempting to upgrade " + "location settings "
                    )
                    try {
                        val rae = it as ResolvableApiException
                        rae.startResolutionForResult(getContext() as Activity,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (sie: IntentSender.SendIntentException) {
                        Log.i("LocationManager", "PendingIntent unable to execute request.")
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    val errorMessage =
                        "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                    Log.e("LocationManager", errorMessage)
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun stopLocationUpdates() {
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }


}