package com.example.kbbqreview

import android.location.Location
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow

class MapViewModel {


    val textFieldState = mutableStateOf("")

    fun onTextFieldChange(query: String) {
        this.textFieldState.value = query
    }
    val cameraPosition = mutableStateOf(LatLng(0.0, 0.0))

    fun changeCameraPosition(location: LocationDetails?) {
        println("BEFORE THE IF STATEMENT")
        location?.let {
            cameraPosition.value = LatLng(location.latitude, location.longitude)
        }
    }

}