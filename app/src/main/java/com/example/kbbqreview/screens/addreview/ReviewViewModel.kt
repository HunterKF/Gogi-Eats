package com.example.kbbqreview.screens.addreview

import android.content.Context
import android.location.Address
import android.location.Geocoder
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.kbbqreview.data.photos.Photo
import java.text.SimpleDateFormat
import java.util.*

class ReviewViewModel : ViewModel() {
    val restaurantNameState = mutableStateOf("")
    val reviewComment = mutableStateOf("")
    fun onTextFieldChange(textValue: MutableState<String>, query: String) {
        textValue.value = query
    }
    val valueMeat = mutableStateOf(2)

    val valueBanchan = mutableStateOf(2)

    val valueAmenities = mutableStateOf(2)

    val valueAtmosphere = mutableStateOf(2)

    val totalValue = mutableStateOf(0)

    val newMarkerPositionLatReview = mutableStateOf(0.0)
    val newMarkerPositionLngReview = mutableStateOf(0.0)

    val stateLat = mutableStateOf("")

    val stateLng = mutableStateOf("")
    val address = mutableStateOf("")

    val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
    val currentDate = sdf.format(Date())

    fun changeLocation(latitude: Double, longitude: Double, context: Context) {
        newMarkerPositionLatReview.value = latitude
        newMarkerPositionLngReview.value = longitude
        address.value = getAddressFromLocation(context, latitude, longitude)
    }
    fun addValues() {
        totalValue.value = valueMeat.value + valueAmenities.value + valueAtmosphere.value + valueBanchan.value
    }

    fun getAddressFromLocation(context: Context, lat: Double, long: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        var addresses: List<Address>? = null
        val address: Address?
        var addressText = ""

        try {
            addresses = geocoder.getFromLocation(
                lat,
                long,
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
        restaurantNameState.value = ""
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