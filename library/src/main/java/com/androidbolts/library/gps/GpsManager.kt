package com.androidbolts.library.gps

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.IntentSender
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.androidbolts.library.LocationModel
import com.androidbolts.library.R
import com.androidbolts.library.utils.LocationConstants
import com.androidbolts.library.utils.LocationConstants.REQUEST_CHECK_SETTINGS
import com.androidbolts.library.utils.LocationConstants.TIME_OUT_NONE
import com.androidbolts.library.utils.showLoadingDialog
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

class GpsManager private constructor() : GpsProvider() {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var mSettingsClient: SettingsClient
    private lateinit var mLocationRequest: LocationRequest
    private var mLocationCallback: LocationCallback? = null
    private var mCurrentLocation: Location? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var dialog: AlertDialog? = null
    private var mRequestingLocationUpdates: Boolean = false

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
        if (!mRequestingLocationUpdates) {
            startLocationUpdates()
            mRequestingLocationUpdates = true
            Log.d("Tag", "onResume called")
        }
    }

    override fun onPause() {
        stopLocationUpdates()
        Log.d("Tag", "onPause called")
    }

    override fun onDestroy() {
        stopLocationUpdates()
        Log.d("Tag", "onDestroy called")
    }

    override fun get() {
        getLocation()
    }

    private fun getLocation() {

        when {
            getFragment() != null -> {
                mFusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(getFragment()?.activity!!)
                mSettingsClient =
                    LocationServices.getSettingsClient(getFragment()!!.requireActivity())
            }
            getActivity() != null -> {
                mFusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(getActivity()!!)
                mSettingsClient = LocationServices.getSettingsClient(getActivity()!!)
            }
            else -> Log.d("LocationManager", "Host is invalid.")
        }
        setupLocationBasic()
    }

    private fun setupLocationBasic() {
        createLocationRequest()
        createLocationCallback()
        buildLocationSettingsRequest()
    }

    private fun createLocationRequest() {
        if (mCurrentLocation == null) {
            showDialog()
        }
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
                mCurrentLocation?.let { currentLocation ->
                    val locationModel = LocationModel(
                        System.currentTimeMillis(),
                        currentLocation.latitude,
                        currentLocation.longitude
                    )
                    getPrefs()?.setLocationModel(locationModel)
                }
                if (isLocationAvailable()) {
                    dismissDialog()
                }

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
//            stopLocationUpdates()
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
                        when {
                            getFragment() != null -> rae.startResolutionForResult(
                                getFragment()?.requireActivity(),
                                REQUEST_CHECK_SETTINGS
                            )
                            getActivity() != null -> rae.startResolutionForResult(
                                getActivity(),
                                REQUEST_CHECK_SETTINGS
                            )
                            else -> Log.d("Invalid host","Host is invalid.")
                        }

                    } catch (sie: IntentSender.SendIntentException) {
                        Log.i("LocationManager", "PendingIntent unable to execute request.")
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    val errorMessage =
                        "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                    Log.e("LocationManager", errorMessage)
                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show()
                    mRequestingLocationUpdates = false
                }
            }
        }
    }

    private fun stopLocationUpdates() {
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
        mRequestingLocationUpdates = false
    }

    private fun showDialog() {
        if (isLoadingSet()) {
            getActivity()?.let { context ->
                dialog = showLoadingDialog(context, "Fetching Location", "Please wait...",
                    false, onPositiveButtonClicked = {
                        setupLocationBasic()
                    }, onNegativeButtonClicked = {
                        stopLocationUpdates()
                        dismissDialog()
                    })

                dialog?.setOnShowListener {
                    val posButton = dialog?.getButton(Dialog.BUTTON_POSITIVE)
                    val negButton = dialog?.getButton(Dialog.BUTTON_NEGATIVE)
                    posButton?.visibility = View.GONE
                    negButton?.visibility = View.GONE
                    if (getTimeOut() != TIME_OUT_NONE) {
                        Handler().postDelayed({
                            posButton?.visibility = View.VISIBLE
                            negButton?.visibility = View.VISIBLE
                            updateDialog()
                        }, getTimeOut())
                    }
                }
                dialog?.show()
            }
        }
    }

    private fun updateDialog() {
        dialog?.findViewById<TextView>(R.id.title)?.text = "Gps problem"
        dialog?.findViewById<TextView>(R.id.message)?.text =
            "We couldn't fetch your current location."
        dialog?.findViewById<ProgressBar>(R.id.progress_circular)?.visibility = View.GONE
    }

    private fun dismissDialog() {
        dialog?.dismiss()
    }

    private fun isLocationAvailable(): Boolean {
        return mCurrentLocation != null && mCurrentLocation!!.latitude > 0.0 && mCurrentLocation!!.longitude > 0.0
    }
}