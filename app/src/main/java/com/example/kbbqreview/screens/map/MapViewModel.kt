package com.example.kbbqreview.screens.map

import android.content.Context
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.kbbqreview.Post
import com.example.kbbqreview.TAG
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.map.location.LocationDetails
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

class MapViewModel: ViewModel() {



    private val cameraPosition = mutableStateOf(LatLng(0.0, 0.0))

    fun changeCameraPosition(location: LocationDetails?) {
        location?.let {
            cameraPosition.value = LatLng(location.latitude, location.longitude)
        }
    }

    val newMarkerPositionLat = mutableStateOf(0.0)
    val newMarkerPositionLng = mutableStateOf(0.0)
    val newMarkerState = mutableStateOf(false)

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
                                authorDisplayName = documentSnapshot.getString("author_id").orEmpty(),
                                authorText = documentSnapshot.getString("author_comment").orEmpty(),
                                restaurantName = documentSnapshot.getString("restaurant_name")
                                    .orEmpty(),
                                location = documentSnapshot.getGeoPoint("location"),
                                valueMeat = documentSnapshot.getLong("value_meat")!!.toInt(),
                                valueSideDishes = documentSnapshot.getLong("value_side_dishes")!!,
                                valueAtmosphere = documentSnapshot.getLong("value_atmosphere")!!,
                                valueAmenities = documentSnapshot.getLong("value_amenities")!!,
                                photoList = getPhotos(firebaseId)
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

    @OptIn(InternalCoroutinesApi::class)
    private fun getPhotos(firebaseId: String): List<Photo> {
        Log.d(TAG, "Captain, we who are about to start salute you! Onward, to the photos!")
        Log.d(TAG, "Before we depart, we would like to check our id... ID: $firebaseId")

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
    fun bitmapDescriptorFromVector(
        context: Context,
        vectorResId: Int
    ): BitmapDescriptor? {

        // retrieve the actual drawable
        val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bm = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        // draw it onto the bitmap
        val canvas = android.graphics.Canvas(bm)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bm)
    }
    fun distanceInKm(distInMeters: Float): String {
        val distInKilometers = distInMeters / 1000
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        return df.format(distInKilometers)
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }
    private fun roundOffDecimal(number: Double): Double? {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.FLOOR
        return df.format(number).toDouble()
    }

    fun distanceText(distance: Float): String {
        val distanceString: String

        if (distance < 1000)
            if (distance < 1)
                distanceString = String.format(Locale.US, "%dm", 1)
            else
                distanceString = String.format(Locale.US, "%dm", Math.round(distance))
        else if (distance > 10000)
            if (distance < 1000000)
                distanceString = String.format(Locale.US, "%dkm", Math.round(distance / 1000))
            else
                distanceString = "FAR"
        else
            distanceString = String.format(Locale.US, "%.2fkm", distance / 1000)

        return distanceString
    }

}