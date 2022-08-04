package com.example.kbbqreview.screens.map

import androidx.compose.runtime.mutableStateOf
import com.example.kbbqreview.screens.map.location.LocationDetails
import com.google.android.gms.maps.model.LatLng

class MapViewModel {



    private val cameraPosition = mutableStateOf(LatLng(0.0, 0.0))

    fun changeCameraPosition(location: LocationDetails?) {
        location?.let {
            cameraPosition.value = LatLng(location.latitude, location.longitude)
        }
    }

    val newMarkerPositionLat = mutableStateOf(0.0)
    val newMarkerPositionLng = mutableStateOf(0.0)
    val newMarkerState = mutableStateOf(false)

}