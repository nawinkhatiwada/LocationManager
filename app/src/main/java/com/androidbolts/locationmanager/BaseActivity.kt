package com.androidbolts.locationmanager

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidbolts.library.LocationListener
import com.androidbolts.library.LocationManager
import com.androidbolts.library.utils.LocationConstants

abstract class BaseActivity: AppCompatActivity(),LocationListener {
    private var locationManager: LocationManager?=null

     fun getLocation(){
        locationManager?.let {
            locationManager?.get()
        }.orElse {
            throw IllegalStateException("locationManager is null. " + "Make sure you initialize LocationManager before attempting to getLocation.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = LocationManager.Builder(this)
            .setListener(this)
            .setRequestTimeOut(LocationConstants.TIME_OUT_SHORT)
            .build()
    }

    override fun onResume() {
        super.onResume()
        locationManager?.onResume()
    }
    override fun onPause() {
        super.onPause()
        locationManager?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationManager?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionGranted(alreadyHadPermission: Boolean) {
        //override if needed
    }

    override fun onPermissionDenied() {
        //override if needed
    }

    override fun onProviderDisabled(provider: String) {
        //override if needed
    }

    override fun onProviderEnabled(provider: String) {
        //override if needed
    }
}