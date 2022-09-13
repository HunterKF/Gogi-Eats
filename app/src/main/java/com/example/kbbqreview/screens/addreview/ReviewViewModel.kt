package com.example.kbbqreview.screens.addreview

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.data.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import java.text.SimpleDateFormat
import java.util.*

class ReviewViewModel : ViewModel() {

    var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    private fun setUser(): String {
        var user = ""
        firebaseUser?.let {
             user = it.uid
        }
        return user
    }

    val restaurantNameText = mutableStateOf("")
    val restaurantReviewText = mutableStateOf("")
    fun onTextFieldChange(textValue: MutableState<String>, query: String) {
        textValue.value = query
    }

    val valueMeat = mutableStateOf(2)

    val sideDishes = mutableStateOf(2)

    val valueAmenities = mutableStateOf(2)

    val valueAtmosphere = mutableStateOf(2)

    private val valueTotal = mutableStateOf(0)

    val restaurantLat = mutableStateOf(0.0)
    val restaurantLng = mutableStateOf(0.0)

    val stateLat = mutableStateOf("")

    val stateLng = mutableStateOf("")
    val address = mutableStateOf("")

    val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
    val currentDate = sdf.format(Date())

    fun changeLocation(latitude: Double, longitude: Double, context: Context) {
        restaurantLat.value = latitude
        restaurantLng.value = longitude
        address.value = getAddressFromLocation(context, latitude, longitude)
    }

    fun addValues() {
        valueTotal.value =
            valueMeat.value + valueAmenities.value + valueAtmosphere.value + sideDishes.value
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
        restaurantNameText.value = ""
        valueMeat.value = 2
        sideDishes.value = 2
        valueAtmosphere.value = 2
        valueAmenities.value = 2
        restaurantLat.value = 0.0
        restaurantLng.value = 0.0
        stateLat.value = ""
        stateLng.value = ""
        selectImages.clear()
    }

    fun onSubmitButton(selectImages: SnapshotStateList<Photo>) {
        val SUBMITTAG = "Submit"
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = requireNotNull(Firebase.auth.currentUser) {
                "Tried to create post without a sign in user"
            }

            val restaurantName = restaurantNameText.value
            val firebaseId = ""
            val postLat = restaurantLat.value
            val postLng = restaurantLng.value
            val postReviewText = restaurantReviewText.value
            val postMeat = valueMeat.value
            val postSideDishes = sideDishes.value
            val postAmenities = valueAmenities.value
            val postAtmosphere = valueAtmosphere.value
            val userId = setUser()

            val handle = Firebase.firestore.collection("reviews")
                .add(
                    hashMapOf(
                        "author_id" to currentUser.displayName.orEmpty(),
                        "user_id" to userId,
                        "date_posted" to Date(),
                        "restaurant_name" to restaurantName,
                        "location" to GeoPoint(postLat, postLng),
                        "author_comment" to postReviewText,
                        "value_meat" to postMeat,
                        "value_side_dishes" to postSideDishes,
                        "value_amenities" to postAmenities,
                        "value_atmosphere" to postAtmosphere,
                    )
                )
            handle.addOnSuccessListener { docRef ->
                Log.d(SUBMITTAG, "It has been submitted. Id: ${docRef.id}")
                updateReview(docRef.id)
                uploadPhotos(selectImages, docRef.id)
            }
            handle.addOnFailureListener {
                Log.e(SUBMITTAG, "Saved failed $it")
            }
        }
    }

    private fun updateReview(id: String) {
        val db = Firebase.firestore.collection("reviews").document(id)
        viewModelScope.launch(Dispatchers.IO) {
            db.update("firebase_id", id)
        }
    }

    fun uploadPhotos(selectImages: SnapshotStateList<Photo>, id: String) {
        val storageReference = FirebaseStorage.getInstance().reference
        viewModelScope.launch(Dispatchers.IO) {
            var listIndex = 0
            selectImages.forEach { photo ->
                photo.listIndex = listIndex
                listIndex += 1
                val postId = ""
                val localUri = photo.localUri
                val uri = Uri.parse(photo.localUri)
                val imageRef =
                    storageReference.child("images/${uri.lastPathSegment}")
                val uploadTask = imageRef.putFile(uri)
                uploadTask.addOnSuccessListener {
                    Log.d("Firebase Image", "Image uploaded $imageRef")
                    val downloadUrl = imageRef.downloadUrl
                    downloadUrl.addOnSuccessListener { remoteUri ->
                        photo.remoteUri = remoteUri.toString()
                        updatePhotoData(photo, id)
                    }
                }
                uploadTask.addOnFailureListener {
                    Log.e("Firebase Image", it.message ?: "No message")
                }
            }
        }
    }

    private fun updatePhotoData(photo: Photo, id: String) {
        val localUri = photo.localUri
        val remoteUri = photo.remoteUri
        val listIndex = photo.listIndex
        viewModelScope.launch(Dispatchers.IO) {
            Firebase.firestore.collection("photos")
                .add(
                    hashMapOf(
                        "local_uri" to localUri,
                        "remote_uri" to remoteUri,
                        "post_id" to id,
                        "list_index" to listIndex
                    )
                )
        }

    }

    fun updateSelectedSliderValue(value: MutableState<Int>, sliderPosition: Float) {
        value.value = sliderPosition.toInt()
    }

}


