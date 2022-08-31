package com.example.kbbqreview

import com.example.kbbqreview.data.photos.Photo
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import java.util.*

class Post(
    val timestamp: Date,
    val authorName: String,
    val authorText: String,
    val restaurantName: String,
    val location: GeoPoint?,
    val valueMeat: Int,
    val valueSideDishes: Long,
    val valueAtmosphere: Long,
    val valueAmenities: Long,
    val photoList: List<Photo>
) {

}
