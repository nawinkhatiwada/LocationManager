package com.androidbolts.locationmanager.base

import android.location.Location
import androidx.fragment.app.Fragment
import com.androidbolts.library.LocationListener
import com.androidbolts.library.LocationManager
import com.androidbolts.library.utils.LocationConstants

abstract class BaseFragment : Fragment(), LocationListener {

    private var locationManager: LocationManager? = null

    fun initLocationManager(): LocationManager? {
        locationManager = LocationManager.Builder(requireActivity().applicationContext)
            .showLoading(false)
            .setListener(this)
            .setFragment(this)
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
        if (requestCode == LocationConstants.LOCATION_PERMISSIONS_REQUEST_CODE) {
            locationManager?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onPermissionGranted(alreadyHadPermission: Boolean) {
        //override if needed
    }

    override fun onPermissionDenied() {
        //override if needed
    }

    override fun onLocationChanged(location: Location?) {
    }
}