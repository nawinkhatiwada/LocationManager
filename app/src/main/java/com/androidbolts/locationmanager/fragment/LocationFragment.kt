package com.androidbolts.locationmanager.fragment

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.androidbolts.library.LocationManager
import com.androidbolts.locationmanager.R
import com.androidbolts.locationmanager.base.BaseFragment

class LocationFragment private constructor() : BaseFragment() {

    private var location: Location? = null
    private var locationManager: LocationManager? = null
    private val tvLocation by lazy { activity?.findViewById<TextView>(R.id.tv_current_location) }
    private var isObserverAdded = false

    companion object {
        fun getInstance() = LocationFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_location, container, false)
        val btnGetLocation = view.findViewById<Button>(R.id.btn_get_loc)
        locationManager = initLocationManager()

        btnGetLocation.setOnClickListener {
            getLocation()
        }
        return view
    }

    private fun getLocation() {
        locationManager?.let {
            if (!it.isLoadingSet()) {
                locationManager?.setShowLoading(true)
            }
            if (!isObserverAdded) {
                lifecycle.addObserver(it)
                isObserverAdded = true
            }
            locationManager?.getLocation()
        }
    }

    override fun onLocationChanged(location: Location?) {
        this.location = location
        Log.d("Location fetched:", "${this.location?.latitude}, ${this.location?.longitude}")
        tvLocation?.text =
            "Latitude: ${this.location?.latitude}\nLongitude: ${this.location?.longitude}"
    }
}