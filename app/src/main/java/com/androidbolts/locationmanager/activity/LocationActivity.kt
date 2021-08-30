package com.androidbolts.locationmanager.activity

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.androidbolts.library.LocationManager
import com.androidbolts.locationmanager.R
import com.androidbolts.locationmanager.base.BaseActivity
import com.androidbolts.locationmanager.databinding.ActivityLocationBinding

class LocationActivity : BaseActivity() {
    private lateinit var binding: ActivityLocationBinding
    private var location: Location? = null
    private var locationManager: LocationManager? = null

    companion object {
        fun getIntent(activity: Activity): Intent {
            return Intent(activity, LocationActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = "Location Activity"
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location)
        getLocation()
    }

    private fun getLocation() {
        locationManager = initLocationManager()
        lifecycle.addObserver(locationManager!!)
        locationManager?.getLocation()
    }

    override fun onLocationChanged(location: Location?) {
        this.location = location
        Log.d("Location fetched:", "${this.location?.latitude}, ${this.location?.longitude}")
        binding.tvCurrentLocation?.text = """Latitude: ${this.location?.latitude}\n
                                             Longitude: ${this.location?.longitude}\n
                                             Accuracy: ${this.location?.accuracy}"""
    }
}