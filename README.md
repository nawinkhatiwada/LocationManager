# LocationManager
[![GitHub release](https://img.shields.io/github/v/release/nawinkhatiwada/LocationManager)](https://github.com/nawinkhatiwada/LocationManager/releases/latest)

To get Location this library follows few steps:
<ul>
<li> Checks for the permission </li>
<li> Ask for the permission at runtime </li>
<li> Check whether user granted the permissions or not </li>
<li> If granted, starts fetching the location </li>
</ul>

## How to use

Add following line on root-level `build.gradle` file.

```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  ```
  Add lastest dependency on app-level `build.gradle`. You can always see [Latest Release](https://github.com/nawinkhatiwada/LocationManager/releases/latest) from here.

  ```
  dependencies {
	        implementation 'com.github.nawinkhatiwada:LocationManager:xyz'
	}
  ```
  After syncing the project, initialize the location manager. Implement LocationListener of package `com.androidbolts.library.LocationListener` to override some required methods for fetching the location. You must add `locationManager` to the lifecycler observer in order to get the location. If you don't want to showProgressbar while fetching the location, you can set `showLoading(false)`. You can also change the timeout duration using `setRequestTimeOut(LocationConstants.TIME_OUT_LONG)`. Use `LocationConstants.TIMEOUT_NONE` if you want to disable timeout.
 You can see the sample project for proper implementation.

  
  ```
  class MainActivity: AppCompatActivity(), LocationListener {
     private var locationManager: LocationManager ?= null
     private var location:Location ?= null

override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
	initLocationManager()
    }

    fun initLocationManager(){
        locationManager = LocationManager.Builder(this)
            .showLoading(true)
            .setListener(this)
            .setRequestTimeOut(LocationConstants.TIME_OUT_LONG)
            .build()
	    locationManager?.let {
               lifecycle.addObserver(it)
            }
	locationManager?.getLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == LocationConstants.LOCATION_PERMISSIONS_REQUEST_CODE) {
            locationManager?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onPermissionGranted(alreadyHadPermission: Boolean) {
        //override if needed
    }

    override fun onPermissionDenied() {
        //override if needed
    }
    
    override fun onLocationChanged(location: Location?) {
        this.location = location
        Log.d("Location fetched:", "${this.location?.latitude}, ${this.location?.longitude}")
        tvLocation.text = "Latitude: ${this.location?.latitude}\nLongitude: ${this.location?.longitude}"
    }
 ```
  
  # License #
```text
   Copyright 2019 nawinkhatiwada

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   ```
