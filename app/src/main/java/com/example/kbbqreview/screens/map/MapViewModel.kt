package com.example.kbbqreview.screens.map

import android.content.Context
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.kbbqreview.data.firestore.Post
import com.example.kbbqreview.TAG
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.map.location.LocationDetails
import com.facebook.internal.Mutable
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class MapViewModel : ViewModel() {




    val newMarkerPositionLat = mutableStateOf(0.0)
    val newMarkerPositionLng = mutableStateOf(0.0)
    val newMarkerState = mutableStateOf(false)

    val singlePost = mutableStateOf(
        Post(
            timestamp = Date(),
            firebaseId = "",
            userId = "",
            authorDisplayName = "",
            authorText =  "",
            restaurantName =  "",
            location = GeoPoint(32.0, 123.0),
            valueMeat = 0,
            valueSideDishes =0,
            valueAtmosphere =0,
            valueAmenities = 0,
            photoList = listOf(),
            distance = 0.0
        )
    )

    fun observePosts(): Flow<List<Post>> {
        return callbackFlow {
            val listener = Firebase.firestore
                .collection("reviews").addSnapshotListener { value, error ->
                    if (error != null) {
                        println("AN ERROR HAS OCCURRED. PLEASE CONSULT A GOD.")
                        close(error)
                    } else if (value != null) {
                        val posts = value.map { documentSnapshot ->
                            val firebaseId = documentSnapshot.id
                            Post(
                                timestamp = documentSnapshot.getDate("date_posted") ?: Date(),
                                userId = Firebase.auth.currentUser.toString(),
                                firebaseId = documentSnapshot.getString("firebase_id").orEmpty(),
                                authorDisplayName = documentSnapshot.getString("author_id")
                                    .orEmpty(),
                                authorText = documentSnapshot.getString("author_comment").orEmpty(),
                                restaurantName = documentSnapshot.getString("restaurant_name")
                                    .orEmpty(),
                                location = documentSnapshot.getGeoPoint("location"),
                                valueMeat = documentSnapshot.getLong("value_meat")!!.toInt(),
                                valueSideDishes = documentSnapshot.getLong("value_side_dishes")!!
                                    .toInt(),
                                valueAtmosphere = documentSnapshot.getLong("value_atmosphere")!!
                                    .toInt(),
                                valueAmenities = documentSnapshot.getLong("value_amenities")!!
                                    .toInt(),
                                photoList = getPhotos(firebaseId),
                                distance = 0.0
                            )
                        }.sortedByDescending { it.timestamp }
                        trySend(posts)
                        //TODO Handle posts
                    }
                }
            awaitClose {
                listener.remove()
            }

        }
    }

    private fun getPhotos(firebaseId: String): List<Photo> {
        Log.d(TAG, "MapVM")
        Log.d(TAG, "MapVM: Before we depart, we would like to check our id... ID: $firebaseId")

        val photoList = mutableListOf<Photo>()
        val db = Firebase.firestore
        db.collection("photos")
            .whereEqualTo("post_id", firebaseId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d(TAG, "Sir, we arrived at the photo, but it's empty!")
                    Thread.sleep(1000)
                }
                result.forEach { documentSnapshot ->
                    val photo = Photo(
                        localUri = documentSnapshot.getString("local_uri").orEmpty(),
                        remoteUri = documentSnapshot.getString("remote_uri").orEmpty(),
                        firebaseId = documentSnapshot.getString("post_id").orEmpty(),
                        listIndex = documentSnapshot.getLong("list_index")!!.toInt()
                    )
                    photoList.add(photo)
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "We regret to inform you that it has failed. Printing out failure...")
                Log.d(TAG, "${it.message}")
            }
        return photoList
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

        return addressText
    }



    fun distanceInKm(distInMeters: Float): Double {
        val distInKilometers = distInMeters / 1000
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        return df.format(distInKilometers).toDouble()
    }
}


