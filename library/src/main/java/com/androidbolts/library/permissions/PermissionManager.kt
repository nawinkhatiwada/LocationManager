package com.androidbolts.library.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androidbolts.library.BuildConfig
import com.androidbolts.library.utils.ContextProcessor
import com.androidbolts.library.utils.LocationConstants.LOCATION_PERMISSIONS_REQUEST_CODE

abstract class PermissionManager {
    private lateinit var listener: PermissionListener
    private lateinit var contextProcessor: ContextProcessor

    fun setListener(listener: PermissionListener) {
        this.listener = listener
    }
    fun setContextProcessor(context:Context){
        this.contextProcessor = ContextProcessor(context)
    }

    fun hasPermission(): Boolean {
        contextProcessor.context?.let { context ->
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    fun requestPermissions() {
        contextProcessor.context?.let { context ->
            ActivityCompat.requestPermissions(
                context as Activity ,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    fun onPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == LOCATION_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    listener.onPermissionDenied()
                }
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> {
                    listener.onPermissionGranted()
                }

                else -> {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts(
                        "package",
                        BuildConfig.LIBRARY_PACKAGE_NAME, null
                    )
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    contextProcessor.context?.startActivity(intent)
                }
            }
        }
    }

}