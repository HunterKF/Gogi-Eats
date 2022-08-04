package com.example.kbbqreview.screens.addreview

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel

class ReviewViewModel : ViewModel() {
    val textFieldState = mutableStateOf("")
    val valueMeat = mutableStateOf(0)

    val valueBanchan = mutableStateOf(0)

    val valueAmenities = mutableStateOf(0)

    val valueAtmosphere = mutableStateOf(0)

    val newMarkerPositionLatReview = mutableStateOf(0.0)
    val newMarkerPositionLngReview = mutableStateOf(0.0)


}