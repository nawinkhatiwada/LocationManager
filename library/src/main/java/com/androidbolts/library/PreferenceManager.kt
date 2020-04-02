package com.androidbolts.library

import android.content.Context
import android.content.SharedPreferences
import com.androidbolts.library.utils.ContextProcessor
import com.androidbolts.library.utils.LocationConstants.KEY_LOCATION_MODEL
import com.androidbolts.library.utils.LocationConstants.PREF_FILE
import com.google.gson.Gson

internal class PreferenceManager internal constructor(){
    private lateinit var contextProcessor: ContextProcessor
    private var gson: Gson?=null

    private val preferenceManager: SharedPreferences  by lazy {
        this.contextProcessor.context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
    }

    companion object{
        private var prefs: PreferenceManager ?= null
        fun getInstance():PreferenceManager?{
            if (prefs == null){
                prefs = PreferenceManager()
            }
            return prefs
        }
    }

    fun setContextProcessor(contextProcessor: ContextProcessor) {
        this.contextProcessor = contextProcessor
    }

    internal fun setLocationModel(locationModel: LocationModel) {
        val json = getGson().toJson(locationModel)
        preferenceManager.edit().putString(KEY_LOCATION_MODEL,json).apply()
    }
    internal fun getLocationModel():LocationModel?{
        val json = preferenceManager.getString(KEY_LOCATION_MODEL,"")
        return getGson().fromJson(json, LocationModel::class.java)
    }

    private fun getGson(): Gson{
        if (gson == null) {
            gson = Gson()
        }
        return gson!!
    }
}