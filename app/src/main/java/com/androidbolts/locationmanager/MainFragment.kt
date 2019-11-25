package com.androidbolts.locationmanager

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.androidbolts.library.LocationManager

class MainFragment private constructor() : BaseFragment() {

    private var location: Location? = null
    private var locationManager: LocationManager? = null
    private val tvLocation by lazy { activity?.findViewById<TextView>(R.id.tv_current_location) }

    companion object {
        fun getInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
        tvLocation?.text =
            "Latitude: ${this.location?.latitude}\nLongitude: ${this.location?.longitude}"
    }
}