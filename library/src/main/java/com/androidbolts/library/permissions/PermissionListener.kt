package com.androidbolts.library.permissions

internal interface PermissionListener {
    fun onPermissionGranted()
    fun onPermissionDenied()
}