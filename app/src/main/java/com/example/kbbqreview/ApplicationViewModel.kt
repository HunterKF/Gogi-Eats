package com.example.kbbqreview

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.kbbqreview.data.location.LocationDetails
import com.example.kbbqreview.data.location.LocationLiveData
import kotlinx.coroutines.flow.MutableStateFlow

class ApplicationViewModel(application: Application) : AndroidViewModel(application) {
    private val locationLiveData = LocationLiveData(application)
    fun getLocationLiveData() = locationLiveData

    fun startLocationUpdates() {
        locationLiveData.startLocationUpdates()
    }



}