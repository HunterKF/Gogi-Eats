package com.example.kbbqreview.screens.addreview

import android.content.Context
import android.location.Address
import android.location.Geocoder
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.kbbqreview.data.photos.Photo
import java.util.*

class ReviewViewModel : ViewModel() {
    val textFieldState = mutableStateOf("")
    val valueMeat = mutableStateOf(2)

    val valueBanchan = mutableStateOf(2)

    val valueAmenities = mutableStateOf(2)

    val valueAtmosphere = mutableStateOf(2)

    val newMarkerPositionLatReview = mutableStateOf(0.0)
    val newMarkerPositionLngReview = mutableStateOf(0.0)

    val stateLat = mutableStateOf("")

    val stateLng = mutableStateOf("")
    val address = mutableStateOf("")

    fun changeLocation(latitude: Double, longitude: Double, context: Context) {
        newMarkerPositionLatReview.value = latitude
        newMarkerPositionLngReview.value = longitude
        address.value = getAddressFromLocation(context)
    }

    fun getAddressFromLocation(context: Context): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        var addresses: List<Address>? = null
        val address: Address?
        var addressText = ""

        try {
            addresses = geocoder.getFromLocation(
                newMarkerPositionLatReview.value,
                newMarkerPositionLngReview.value,
                1
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        address = addresses?.get(0)
        addressText = address?.getAddressLine(0) ?: ""
        stateLng.value = address?.longitude.toString()
        stateLat.value = address?.latitude.toString()


        return addressText
    }

    fun clearValues(selectImages: SnapshotStateList<Photo>) {
        textFieldState.value = ""
        valueMeat.value = 2
        valueBanchan.value = 2
        valueAtmosphere.value = 2
        valueAmenities.value = 2
        newMarkerPositionLatReview.value = 0.0
        newMarkerPositionLngReview.value = 0.0
        stateLat.value = ""
        stateLng.value = ""
        selectImages.clear()
    }


}