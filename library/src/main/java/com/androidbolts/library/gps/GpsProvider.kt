package com.androidbolts.library.gps

import android.app.Activity
import android.content.Context
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.androidbolts.library.LocationListener
import com.androidbolts.library.PreferenceManager
import com.androidbolts.library.utils.ContextProcessor
import com.androidbolts.library.utils.LocationConstants.TIME_OUT_NONE
import java.lang.ref.WeakReference

abstract class GpsProvider {
    private lateinit var weakContextProcessor: WeakReference<ContextProcessor>
    private var locationListener: LocationListener? = null
    private var showDialog: Boolean = false
    private var timeOut: Long = TIME_OUT_NONE
    private var prefs: PreferenceManager? = null
    abstract fun onCreate()
    abstract fun onResume()
    abstract fun onPause()
    abstract fun onDestroy()
    abstract fun get()
    abstract fun enableGps()

    fun setContextProcessor(contextProcessor: ContextProcessor) {
        this.weakContextProcessor = WeakReference(contextProcessor)
    }

    fun setLocationListener(locationListener: LocationListener?) {
        this.locationListener = locationListener
    }

    fun setShowLoading(show: Boolean) {
        this.showDialog = show
    }

    @Nullable
    protected fun getContext(): Context? {
        return this.weakContextProcessor.get()?.context
    }

    @Nullable
    protected fun getActivity(): Activity? {
        return if (weakContextProcessor.get() == null) null else weakContextProcessor.get()!!.activity
    }

    @Nullable
    protected fun getFragment(): Fragment? {
        return if (weakContextProcessor.get() == null) null else weakContextProcessor.get()!!.fragment
    }

    fun getLocationListener(): LocationListener? {
        return this.locationListener
    }

    fun isLoadingSet(): Boolean {
        return this.showDialog
    }

    fun setTimeOut(timeOut: Long) {
        if (timeOut < 0) {
            throw Exception("Timeout can't be negative value.")
        }
        this.timeOut = timeOut
    }

    fun getTimeOut(): Long {
        return this.timeOut
    }

    internal fun setPrefs(prefs: PreferenceManager?) {
        this.prefs = prefs
    }

    internal fun getPrefs(): PreferenceManager? {
        return this.prefs
    }
}