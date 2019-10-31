package com.androidbolts.locationmanager

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.androidbolts.library.LocationListener
import android.view.Gravity
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import com.androidbolts.library.LocationManager

class MainActivity : BaseActivity() {
    private var location:Location ?= null
    private var locationManager: LocationManager ?= null
    private val tvLocation by lazy { findViewById<TextView>(R.id.tv_current_location) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getLocation()
    }
    private fun getLocation(){
        locationManager = initLocationManager()
        locationManager?.let {
            lifecycle.addObserver(it)
        }
        locationManager?.getLocation()
        //TODO solve null
        val a = locationManager?.getLastUpdatedLocation()
        Log.d("LocationModel", a.toString())
    }

    override fun onLocationChanged(location: Location?) {
        this.location = location
        Log.d("Location fetched:", "${this.location?.latitude}, ${this.location?.longitude}")
        tvLocation.text = "Latitude: ${this.location?.latitude}\nLongitude: ${this.location?.longitude}"
    }
}
