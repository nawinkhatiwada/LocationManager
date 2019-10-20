package com.androidbolts.library

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.androidbolts.library.gps.GpsManager
import com.androidbolts.library.gps.GpsProvider
import com.androidbolts.library.permissions.PermissionListener
import com.androidbolts.library.permissions.PermissionProvider
import com.androidbolts.library.utils.ContextProcessor
import com.androidbolts.library.utils.LocationConstants

class LocationManager private constructor(
    private val locationListener: LocationListener?,
    contextProcessor: ContextProcessor,
    private val timeOut: Int = LocationConstants.TIME_OUT_NONE
): PermissionListener {
    private var permissionManager = PermissionProvider.getPermissionManager()
    private var dialog: AlertDialog ?= null
    private var gpsProvider: GpsProvider

    init {
        this.permissionManager.setListener(this)
        this.permissionManager.setContextProcessor(contextProcessor.context)
        this.gpsProvider = GpsManager.getGpsManager()
        this.gpsProvider.setContextProcessor(contextProcessor)
        this.gpsProvider.setLocationListener(locationListener)
    }

     class Builder constructor(context: Context) {

        private lateinit var locationListener: LocationListener
        private var timeOut: Int = LocationConstants.TIME_OUT_NONE
        private var contextProcessor: ContextProcessor = ContextProcessor(context)

        fun setListener(listener: LocationListener): Builder {
            this.locationListener = listener
            return this
        }

        fun setRequestTimeOut(timeOut: Int):Builder {
            this.timeOut = timeOut
            return this
        }

        fun build(): LocationManager {
            return LocationManager(locationListener, contextProcessor, timeOut)
        }
    }

    fun get() {
        askForPermission()
    }

    private fun askForPermission() {
        if(permissionManager.hasPermission()){
            permissionGranted(true)
        }else{
            permissionManager.requestPermissions()
        }
    }

    private fun permissionGranted(alreadyHadPermission:Boolean){
        locationListener?.onPermissionGranted(alreadyHadPermission)
        gpsProvider.get(hasTimeOut())
    }

    private fun onPermissionGrantedFailed(){
        locationListener?.let {
            locationListener.onPermissionDenied()
        }
    }

    private fun hasTimeOut():Boolean{
        return timeOut != LocationConstants.TIME_OUT_NONE
    }

    fun onResume(){
        locationListener?.let {
            gpsProvider.onResume()
        }
    }

    fun onPause(){
        gpsProvider.onPause()
    }

    fun onDestroy(){
        gpsProvider.onDestroy()
    }

    //TODO shift this function to other class
//    private fun showDialog(){
//        when(timeOut){
//            LocationConstants.TIME_OUT_NONE -> {
//                //NO-OP
//            }
//            LocationConstants.TIME_OUT_LONG or LocationConstants.TIME_OUT_SHORT -> {
//                dialog = showLoadingDialog(contextProcessor.context,"Fetching Location", "Please wait...", false)
//                dialog?.show()
//            }
//        }
//    }
//
//    private fun dismissDialog(){
//        dialog?.dismiss()
//    }

    override fun onPermissionGranted() {
        permissionGranted(false)
    }

    override fun onPermissionDenied() {
        onPermissionGrantedFailed()
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionManager.onPermissionResult(requestCode, permissions,grantResults)
    }
}