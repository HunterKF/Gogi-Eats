package com.example.kbbqreview.data.firestore

import androidx.compose.runtime.MutableState
import com.example.kbbqreview.data.photos.Photo
import com.google.firebase.firestore.GeoPoint
import java.util.*

class EditingPost(
    val timestamp: Date,
    val firebaseId: String,
    val userId: String,
    val authorDisplayName: String,
    val authorText: MutableState<String>,
    var restaurantName: MutableState<String>,
    var location: GeoPoint?,
    var valueMeat: MutableState<Int>,
    var valueSideDishes:MutableState<Int>,
    var valueAtmosphere:MutableState<Int>,
    var valueAmenities: MutableState<Int>,
    var photoList: List<Photo>,
    var distance: Double
) {
}