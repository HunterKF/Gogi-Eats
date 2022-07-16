package com.example.kbbqreview

import android.app.Application
import android.location.Location
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow

class ApplicationViewModel(application: Application) : AndroidViewModel(application) {
    private val locationLiveData = LocationLiveData(application)
    fun getLocationLiveData() = locationLiveData
    val initialLocation = getLocationLiveData()
    fun startLocationUpdates() {
        locationLiveData.startLocationUpdates()
    }
    val busan = LocationDetails(36.1612, 129.1561)
    val currentLocation = MutableStateFlow<LocationDetails>(busan)


}