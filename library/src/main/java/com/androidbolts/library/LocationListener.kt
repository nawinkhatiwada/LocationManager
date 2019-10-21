package com.androidbolts.library

import android.location.Location

interface LocationListener {
    fun onLocationChanged(location:Location?)
    fun onPermissionGranted(alreadyHadPermission: Boolean)
    fun onPermissionDenied()
//    fun onProviderEnabled(provider:String)
//    fun onProviderDisabled(provider: String)
}