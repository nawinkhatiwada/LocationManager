package com.androidbolts.locationmanager

import androidx.appcompat.app.AppCompatActivity
import com.androidbolts.library.LocationListener
import com.androidbolts.library.LocationManager
import com.androidbolts.library.utils.LocationConstants

abstract class BaseActivity: AppCompatActivity(), LocationListener {
     private var locationManager: LocationManager ?= null

    fun initLocationManager():LocationManager?{
        locationManager = LocationManager.Builder(this)
            .showLoading(true)
            .setListener(this)
            .setRequestTimeOut(LocationConstants.TIME_OUT_LONG)
            .build()
        return locationManager
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == LocationConstants.LOCATION_PERMISSIONS_REQUEST_CODE) {
            locationManager?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onPermissionGranted(alreadyHadPermission: Boolean) {
        //override if needed
    }

    override fun onPermissionDenied() {
        //override if needed
    }
}