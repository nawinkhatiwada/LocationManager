package com.androidbolts.library.gps

import android.content.Context
import android.content.SharedPreferences
import com.androidbolts.library.LocationListener
import com.androidbolts.library.PreferenceManager
import com.androidbolts.library.utils.ContextProcessor
import com.androidbolts.library.utils.LocationConstants.TIME_OUT_NONE

abstract class GpsProvider {
    private lateinit var contextProcessor: ContextProcessor
    private var locationListener: LocationListener?=null
    private var showDialog:Boolean = false
    private var timeOut:Int = TIME_OUT_NONE
    private var prefs: PreferenceManager ?=null
    abstract fun onResume()
    abstract fun onPause()
    abstract fun onDestroy()
    abstract fun get()

    fun setContextProcessor(contextProcessor: ContextProcessor) {
        this.contextProcessor = contextProcessor
    }
    fun setLocationListener(locationListener: LocationListener?){
        this.locationListener = locationListener
    }

    fun setShowLoading(show:Boolean){
        this.showDialog = show
    }

    fun getContext():Context{
        return this.contextProcessor.context
    }

    fun getLocationListener():LocationListener?{
        return this.locationListener
    }

    fun isLoadingSet(): Boolean{
        return this.showDialog
    }

    fun setTimeOut(timeOut: Int){
        if(timeOut < 0){
            throw Exception("Timeout can't be negative value.")
        }
        this.timeOut = timeOut

    }
    fun getTimeOut():Int{
        return this.timeOut
    }

    fun setPrefs(prefs: PreferenceManager?){
        this.prefs = prefs
    }
    fun getPrefs():PreferenceManager?{
        return this.prefs
    }
}