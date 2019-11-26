package com.androidbolts.library.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.androidbolts.library.BuildConfig
import com.androidbolts.library.utils.ContextProcessor
import com.androidbolts.library.utils.LocationConstants.LOCATION_PERMISSIONS_REQUEST_CODE
import java.lang.ref.WeakReference

abstract class PermissionManager {
    private lateinit var weakPermissionListener: WeakReference<PermissionListener>
    private lateinit var weakContextProcessor: WeakReference<ContextProcessor>

    internal fun setListener(listener: PermissionListener) {
        this.weakPermissionListener = WeakReference(listener)
    }

    fun setContextProcessor(contextProcessor: ContextProcessor) {
        this.weakContextProcessor = WeakReference(contextProcessor)
    }

    @Nullable
    protected fun getContext(): Context? {
        return if (weakContextProcessor.get() == null) null
        else weakContextProcessor.get()!!.context
    }

    @Nullable
    protected fun getActivity(): Activity? {
        return if (weakContextProcessor.get() == null) null else weakContextProcessor.get()!!.activity
    }

    @Nullable
    protected fun getFragment(): Fragment? {
        return if (weakContextProcessor.get() == null) null else weakContextProcessor.get()!!.fragment
    }

    fun hasPermission(): Boolean {
        getContext()?.let { context ->
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    fun requestPermissions() {

        if (getFragment() != null) {
            getFragment()!!.requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), LOCATION_PERMISSIONS_REQUEST_CODE
            )
        } else if (getActivity() != null) {
            ActivityCompat.requestPermissions(
                getActivity()!!,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSIONS_REQUEST_CODE
            )
        } else {
            Log.d("Permission Denied", "PERMISSION DENIED")
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
                    weakPermissionListener.get()?.onPermissionDenied()
                }
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> {
                    weakPermissionListener.get()?.onPermissionGranted()
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
                    when {
                        getFragment() != null -> getFragment()?.startActivity(intent)
                        getActivity() != null -> getActivity()?.startActivity(intent)
                        else -> Log.d(
                            "Error starting activity",
                            "You can either start location activity from fragment or activity."
                        )
                    }
                }
            }
        }
    }
}