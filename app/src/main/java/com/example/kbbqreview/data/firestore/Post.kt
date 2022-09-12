package com.example.kbbqreview.data.firestore

import com.example.kbbqreview.data.photos.Photo
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import java.util.*

class Post(
    val timestamp: Date,
    val firebaseId: String,
    val userId: String,
    val authorDisplayName: String,
    val authorText: String,
    var restaurantName: String,
    val location: GeoPoint?,
    val valueMeat: Int,
    val valueSideDishes: Int,
    val valueAtmosphere: Int,
    val valueAmenities: Int,
    val photoList: List<Photo>,
    var distance: Double
) {

}
