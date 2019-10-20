package com.androidbolts.locationmanager

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.androidbolts.library.LocationListener
import android.view.Gravity



class MainActivity : BaseActivity(), LocationListener {
    private var location:Location?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getLocation()
    }

    override fun onLocationChanged(location: Location?) {
        this.location = location
        Log.d("Location ayo", "${this.location?.latitude}, ${this.location?.longitude}")
    }

    override fun onPermissionGranted(alreadyHadPermission: Boolean) {
        super.onPermissionGranted(alreadyHadPermission)
        if(alreadyHadPermission){

           val a =  Toast.makeText(this, "Already had permission requesting location updates", Toast.LENGTH_LONG)
            a.setGravity(
                Gravity.TOP or Gravity.START,
                0,
                100
            )
            a.show()
        }
    }
}
