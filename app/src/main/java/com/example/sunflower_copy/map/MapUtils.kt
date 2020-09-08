package com.example.sunflower_copy.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import timber.log.Timber
import java.util.*


//private val TAG = MapsActivityCurrentPlace::class.java.simpleName
val TAG = MapFragment::class.java.simpleName
const val DEFAULT_ZOOM = 15
const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

// Keys for storing activity state.
// [START maps_current_place_state_keys]
const val KEY_CAMERA_POSITION = "camera_position"
const val KEY_LOCATION = "location"
// [END maps_current_place_state_keys]

//private const val HUE_GREEN = BitmapDescriptorFactory.HUE_GREEN //should be 120.0
const val HUE_GREEN = 90.0f
val DEFAULT_LATLNG = LatLng(51.72461, -1.23530)
//val DEFAULT_LATLNG = LatLng(-33.8523341, 151.2106085)

const val MAX_ALPHA = 255
const val MAX_HUE_DEGREES = 360



fun setHomeMarker(map: GoogleMap, homeLatLng: LatLng) {
    val latitude = 51.724609
    val longitude = -1.235296
    //val homeLatLng = DEFAULT_LATLNG
    //val homeLatLng = LatLng(homeLocation.latitude,homeLocation.longitude)
    val zoomLevel = 18f

    val snippet = String.format(
        Locale.getDefault(),
        "Lat: %1$.5f, Long: %2$.5f",
        homeLatLng.latitude,
        homeLatLng.longitude
    )

    map.addMarker(
        MarkerOptions().position(homeLatLng)
            .title("Home")
            .snippet(snippet)
            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_home_black_18dp)))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .zIndex(10f)
    )

}


//
//    private fun isPermissionGranted() : Boolean {
//        return ContextCompat.checkSelfPermission(
//            requireActivity(),
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//    }

//    private fun enableMyLocation() {
//        if (isPermissionGranted()) {
//            map?.isMyLocationEnabled = true
//        }
//        else {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
//                REQUEST_LOCATION_PERMISSION
//            )
//        }
//    }


fun getLocationPermission(context: Context): Boolean {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    var locationPermissionGranted: Boolean = false
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        == PackageManager.PERMISSION_GRANTED) {
        locationPermissionGranted = true
    } else {
        ActivityCompat.requestPermissions(
            context as Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        )
    }

    return locationPermissionGranted
}


fun updateLocationUI(
    context: Context,
    map: GoogleMap,  locationPermissionGranted: Boolean): Location? {
    var lastKnownLocation: Location? = null
    if (map == null) {
        return lastKnownLocation
    }
    try {
        if (locationPermissionGranted) {
            map.isMyLocationEnabled = true
            map.uiSettings?.isMyLocationButtonEnabled = true
        } else {
            map.isMyLocationEnabled = false
            map.uiSettings?.isMyLocationButtonEnabled = false
            lastKnownLocation = null
            //locationPermissionGranted = getLocationPermission(context)
        }
    } catch (e: SecurityException) {
        Log.e("Exception: %s", e.message, e)
    }

    return lastKnownLocation
}




