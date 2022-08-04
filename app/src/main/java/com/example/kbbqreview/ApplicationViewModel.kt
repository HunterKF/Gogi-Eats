package com.example.kbbqreview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.kbbqreview.screens.map.location.LocationLiveData

class ApplicationViewModel(application: Application) : AndroidViewModel(application) {
    private val locationLiveData = LocationLiveData(application)
    fun getLocationLiveData() = locationLiveData

    fun startLocationUpdates() {
        locationLiveData.startLocationUpdates()
    }



}