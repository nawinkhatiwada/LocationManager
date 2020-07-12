package com.androidbolts.library

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.androidbolts.library.gps.GpsManager
import com.androidbolts.library.gps.GpsProvider
import com.androidbolts.library.permissions.PermissionListener
import com.androidbolts.library.permissions.PermissionProvider
import com.androidbolts.library.utils.ContextProcessor
import com.androidbolts.library.utils.ExperimentalSharedPrefs
import com.androidbolts.library.utils.LocationConstants

class LocationManager internal constructor(
    private val locationListener: LocationListener?,
    contextProcessor: ContextProcessor,
    timeOut: Long = LocationConstants.TIME_OUT_NONE,
    showLoading: Boolean
) : PermissionListener, LifecycleObserver {
    private var permissionManager = PermissionProvider.getPermissionManager()
    private var gpsProvider: GpsProvider
    private var prefs: PreferenceManager? = null

    init {
        this.permissionManager.setListener(this)
        this.permissionManager.setContextProcessor(contextProcessor)
        this.gpsProvider = GpsManager.getGpsManager()
        this.gpsProvider.setContextProcessor(contextProcessor)
        this.gpsProvider.setLocationListener(locationListener)
        this.gpsProvider.setShowLoading(showLoading)
        this.gpsProvider.setTimeOut(timeOut)
        this.prefs = PreferenceManager.getInstance()
        this.prefs?.setContextProcessor(contextProcessor)
        this.gpsProvider.setPrefs(this.prefs)
    }

    class Builder (context: Context) {

        private lateinit var locationListener: LocationListener
        private var timeOut: Long = LocationConstants.TIME_OUT_NONE
        private var contextProcessor: ContextProcessor = ContextProcessor(context)
        private var showLoading: Boolean = false
        fun setListener(listener: LocationListener): Builder {
            this.locationListener = listener
            return this
        }

        fun setActivity(activity: Activity): Builder {
            this.contextProcessor.activity = activity
            return this
        }

        fun setFragment(fragment: Fragment): Builder {
            this.contextProcessor.fragment = fragment
            return this
        }

        fun setRequestTimeOut(timeOut: Long): Builder {
            this.timeOut = timeOut
            return this
        }

        fun showLoading(show: Boolean): Builder {
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
        if (permissionManager.hasPermission()) {
            permissionGranted(true)
        } else {
            permissionManager.requestPermissions()
        }
    }

    private fun permissionGranted(alreadyHadPermission: Boolean) {
        locationListener?.onPermissionGranted(alreadyHadPermission)
//        gpsProvider.get()
    }

    private fun onPermissionGrantedFailed() {
        locationListener?.let {
            locationListener.onPermissionDenied()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        locationListener?.let {
            gpsProvider.onCreate()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        locationListener?.let {
            val hasPermission = permissionManager.hasPermission()
            Log.i("Has Permission", hasPermission.toString())
            val isProviderEnabled = permissionManager.isProviderEnabled()
            Log.i("isProviderEnabled", isProviderEnabled.toString())
            if (hasPermission) {
                if (isProviderEnabled) {
                    gpsProvider.onResume()
                }else {
                    gpsProvider.enableGps()
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        locationListener?.let {
            gpsProvider.onPause()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    internal fun onDestroy() {
        locationListener?.let {
            gpsProvider.onDestroy()
        }
    }

    fun setShowLoading(showLoading: Boolean) {
        this.gpsProvider.setShowLoading(showLoading)
    }

    fun isLoadingSet(): Boolean {
        return this.gpsProvider.isLoadingSet()
    }

    @ExperimentalSharedPrefs
    /** This is currently is experimental state so it may not work. */
    fun getLastUpdatedLocation(): LocationModel? {
        return prefs?.getLocationModel()
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
        permissionManager.onPermissionResult(requestCode, permissions, grantResults)
    }
}