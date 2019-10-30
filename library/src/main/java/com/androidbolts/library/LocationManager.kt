package com.androidbolts.library

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.androidbolts.library.gps.GpsManager
import com.androidbolts.library.gps.GpsProvider
import com.androidbolts.library.permissions.PermissionListener
import com.androidbolts.library.permissions.PermissionProvider
import com.androidbolts.library.utils.ContextProcessor
import com.androidbolts.library.utils.LocationConstants

class LocationManager private constructor(
    private val locationListener: LocationListener?,
    contextProcessor: ContextProcessor,
    timeOut: Long = LocationConstants.TIME_OUT_NONE,
    showLoading: Boolean
): PermissionListener, LifecycleObserver {
    private var permissionManager = PermissionProvider.getPermissionManager()
    private var gpsProvider: GpsProvider
    private var prefs: PreferenceManager?=null
    init {
        this.permissionManager.setListener(this)
        this.permissionManager.setContextProcessor(contextProcessor.context)
        this.gpsProvider = GpsManager.getGpsManager()
        this.gpsProvider.setContextProcessor(contextProcessor)
        this.gpsProvider.setLocationListener(locationListener)
        this.gpsProvider.setShowLoading(showLoading)
        this.gpsProvider.setTimeOut(timeOut)
        this.prefs = PreferenceManager.getInstance()
        this.prefs?.setContextProcessor(contextProcessor)
        this.gpsProvider.setPrefs(this.prefs)
    }

     class Builder constructor(context: Context) {

        private lateinit var locationListener: LocationListener
        private var timeOut: Long = LocationConstants.TIME_OUT_NONE
        private var contextProcessor: ContextProcessor = ContextProcessor(context)
        private var showLoading: Boolean = false
        fun setListener(listener: LocationListener): Builder {
            this.locationListener = listener
            return this
        }

         //TODO yesko kam baki
        fun setRequestTimeOut(timeOut: Long):Builder {
            this.timeOut = timeOut
            return this
        }
         fun showLoading(show: Boolean):Builder{
             this.showLoading = show
             return this
         }

        fun build(): LocationManager {
            return LocationManager(locationListener, contextProcessor, timeOut, showLoading)
        }
    }

    fun getLocation() {
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
        gpsProvider.get()
    }

    private fun onPermissionGrantedFailed(){
        locationListener?.let {
            locationListener.onPermissionDenied()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
     fun onResume(){
        locationListener?.let {
            if(permissionManager.hasPermission()) {
                gpsProvider.onResume()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    internal fun onPause(){
        locationListener?.let {
            gpsProvider.onPause()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    internal fun onDestroy(){
        locationListener?.let {
            gpsProvider.onDestroy()
        }
    }

    fun getLastUpdatedLocation(): LocationModel? {
        return  prefs?.getLocationModel()
    }

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