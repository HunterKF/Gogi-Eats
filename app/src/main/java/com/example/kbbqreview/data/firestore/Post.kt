package com.example.kbbqreview.data.firestore

import androidx.compose.runtime.MutableState
import com.example.kbbqreview.data.photos.Photo
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import java.util.*

class Post(
    val timestamp: Date,
    val firebaseId: String,
    val userId: String,
    val authorDisplayName: String,
    val authorText: MutableState<String>,
    var restaurantName: MutableState<String>,
    val location: GeoPoint?,
    var valueMeat: MutableState<Int>,
    var valueSideDishes: MutableState<Int>,
    var valueAtmosphere: MutableState<Int>,
    var valueAmenities: MutableState<Int>,
    val photoList: List<Photo>,
    var distance: Double
) {
    fun deepCopy():Post {
        val JSON = Gson().toJson(this)
        return Gson().fromJson(JSON, Post::class.java)
    }
}
