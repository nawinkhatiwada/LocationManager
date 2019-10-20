package com.androidbolts.library.permissions

interface PermissionListener {
    fun onPermissionGranted()
    fun onPermissionDenied()
}