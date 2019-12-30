package com.androidbolts.library.utils

object LocationConstants {
    const val TIME_OUT_NONE = 0L
    const val TIME_OUT_SHORT = 10000L
    const val TIME_OUT_LONG = 15000L
    const val LOCATION_PERMISSIONS_REQUEST_CODE = 1
    const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
    const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
        UPDATE_INTERVAL_IN_MILLISECONDS / 3
    const val REQUEST_CHECK_SETTINGS = 0x1
    const val PREF_FILE = "LocationManager"
    const val KEY_LOCATION_MODEL = "location_model"



}