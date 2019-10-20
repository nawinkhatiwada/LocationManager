package com.androidbolts.library.gps

import android.content.Context
import com.androidbolts.library.LocationListener
import com.androidbolts.library.utils.ContextProcessor

abstract class GpsProvider {
    private lateinit var contextProcessor: ContextProcessor
    private var locationListener: LocationListener?=null
    abstract fun onResume()
    abstract fun onPause()
    abstract fun onDestroy()
    abstract fun get(hasTimeOut:Boolean)

    fun setContextProcessor(contextProcessor: ContextProcessor) {
        this.contextProcessor = contextProcessor
    }
    fun setLocationListener(locationListener: LocationListener?){
        this.locationListener = locationListener
    }

    fun getContext():Context{
        return this.contextProcessor.context
    }

    fun getLocationListener():LocationListener?{
        return this.locationListener
    }

}