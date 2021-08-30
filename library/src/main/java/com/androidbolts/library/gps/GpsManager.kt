package com.androidbolts.library.gps

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.IntentSender
import android.location.Location
import android.os.CountDownTimer
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
import com.androidbolts.library.utils.orElse
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

internal class GpsManager internal constructor() : GpsProvider() {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mSettingsClient: SettingsClient? = null
    private var mLocationRequest: LocationRequest = LocationRequest()
    private var mLocationCallback: LocationCallback? = null
    private var mCurrentLocation: Location? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var dialog: AlertDialog? = null
    private var mRequestingLocationUpdates: Boolean = false
    private var timer: CountDownTimer? = null

    companion object {
        fun getGpsManager(): GpsManager {
            return GpsManager()
        }
    }

    override fun onCreate() {
        initFusedAndSettingClient()
        createLocationCallback()
        createLocationRequest()
        buildLocationSettingsRequest()
    }

    override fun get() {
        if (!mRequestingLocationUpdates) {
            startLocationUpdates()
            mRequestingLocationUpdates = true
        }
    }

    override fun onResume() {
        get()
        Log.d("Tag", "onResume called")
    }

    override fun onPause() {
        stopLocationUpdates()
        Log.d("Tag", "onPause called")
    }

    override fun onDestroy() {
        stopLocationUpdates()
        mCurrentLocation = null
        Log.d("Tag", "onDestroy called")
    }

    private fun initFusedAndSettingClient() {
        when {
            getFragment() != null -> {
                val activity = getFragment()?.activity
                activity?.let {
                    mFusedLocationClient =
                        LocationServices.getFusedLocationProviderClient(activity)
                    mSettingsClient =
                        LocationServices.getSettingsClient(activity)
                }.orElse {
                    Log.i("Activity:", activity.toString())
                }
            }
            getActivity() != null -> {
                mFusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(getActivity()!!)
                mSettingsClient = LocationServices.getSettingsClient(getActivity()!!)
            }
            else -> Log.i("LocationManager", "Host is invalid.")
        }
    }

    private fun createLocationRequest() {
        mLocationRequest.interval = LocationConstants.UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.fastestInterval =
            LocationConstants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                mCurrentLocation = if (locationResult?.locations.isNullOrEmpty()) {
                    locationResult?.lastLocation
                } else {
                    locationResult?.locations?.first()
                }
                mCurrentLocation?.let { currentLocation ->
                    val locationModel = LocationModel(
                        System.currentTimeMillis(),
                        currentLocation.latitude,
                        currentLocation.longitude,
                        currentLocation.accuracy
                    )
                    getPrefs()?.setLocationModel(locationModel)
                }
                if (isLocationAvailable()) {
                    timer?.cancel()
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
        mLocationRequest.let { locReq ->
            builder.addLocationRequest(locReq)
        }
        mLocationSettingsRequest = builder.build()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mSettingsClient?.checkLocationSettings(mLocationSettingsRequest)?.addOnCompleteListener {
            if (it.isSuccessful) {
                val frag = getFragment()
                val act = getActivity()
                if (mCurrentLocation == null && (act != null || frag != null)) {
                    showDialog()
                }
                mFusedLocationClient?.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback, Looper.myLooper()
                )
            }
        }
    }

    override fun enableGps() {
        mSettingsClient?.checkLocationSettings(mLocationSettingsRequest)?.addOnFailureListener {
            when ((it as ApiException).statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i(
                        "LocationManager",
                        "Location settings are not satisfied. Attempting to upgrade " + "location settings "
                    )
                    try {
                        val rae = it as ResolvableApiException
                        when {
                            getFragment() != null -> {
                                val activity = getFragment()?.activity
                                activity?.let { fragmentActivity ->
                                    rae.startResolutionForResult(
                                        fragmentActivity,
                                        REQUEST_CHECK_SETTINGS
                                    )
                                }
                            }
                            getActivity() != null -> {
                                rae.startResolutionForResult(
                                    getActivity(),
                                    REQUEST_CHECK_SETTINGS
                                )
                            }
                            else -> {
                                Log.d("Invalid host", "Host is invalid.")
                            }
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
                        startLocationUpdates()
                    }, onNegativeButtonClicked = {
                        stopLocationUpdates()
                        dismissDialog()
                    })

                dialog?.setOnShowListener {
                    val posButton = dialog?.getButton(Dialog.BUTTON_POSITIVE)
                    val negButton = dialog?.getButton(Dialog.BUTTON_NEGATIVE)
                    posButton?.visibility = View.GONE
                    negButton?.visibility = View.GONE
                    if (getTimeOut() != TIME_OUT_NONE && mCurrentLocation == null) {
                        timer = object : CountDownTimer(getTimeOut(), 1000) {
                            override fun onFinish() {
                                if (mCurrentLocation == null) {
                                    posButton?.visibility = View.VISIBLE
                                    negButton?.visibility = View.VISIBLE
                                    updateDialog()
                                } else {
                                    dismissDialog()
                                }
                            }

                            override fun onTick(running: Long) {
                            }
                        }
                        timer?.start()
                    }
                }
                dialog?.let { loadingDialog ->
                    if (!loadingDialog.isShowing) {
                        dialog?.show()
                    } else {
                        dialog?.dismiss()
                    }
                }
            }
        }
    }

    private fun updateDialog() {
        dialog?.findViewById<TextView>(R.id.title)?.text =
            getContext()?.getString(R.string.gps_problem)
        dialog?.findViewById<TextView>(R.id.message)?.text =
            getContext()?.getString(R.string.gps_problem_desc)
        dialog?.findViewById<ProgressBar>(R.id.progress_circular)?.visibility = View.GONE
    }

    private fun dismissDialog() {
        dialog?.let {
            if (it.isShowing) {
                dialog?.dismiss()
            }
        }
    }

    private fun isLocationAvailable(): Boolean {
        return mCurrentLocation != null && mCurrentLocation!!.latitude > 0.0 && mCurrentLocation!!.longitude > 0.0
    }
}