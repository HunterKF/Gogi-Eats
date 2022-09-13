package com.example.kbbqreview.screens.profile

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kbbqreview.data.firestore.Post
import com.example.kbbqreview.data.photos.Photo
import com.facebook.AccessToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

sealed class ProfileScreenState {
    object Loading : ProfileScreenState()
    data class Loaded(
        val avatarUrl: String,
        val posts: List<Post>,
    ) : ProfileScreenState()

    object SignInRequired : ProfileScreenState()
}

val TAG = "Profile"

class ProfileViewModel : ViewModel() {


    val editingState = mutableStateOf(false)
    private val mutableState = MutableStateFlow<ProfileScreenState>(
        ProfileScreenState.Loading
    )

    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {

            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {
                observePosts(currentUser)
            } else {
                mutableState.emit(
                    ProfileScreenState.SignInRequired
                )
            }
        }
    }

    var post = mutableStateOf(Post(
        timestamp = Date(),
        firebaseId = "",
        userId = "",
        authorDisplayName = "",
        authorText = mutableStateOf(""),
        restaurantName = mutableStateOf(""),
        location = GeoPoint(0.0, 0.0),
        valueMeat = mutableStateOf(0),
        valueSideDishes = mutableStateOf(0),
        valueAtmosphere = mutableStateOf(0),
        valueAmenities = mutableStateOf(0),
        photoList = listOf(),
        distance = 0.0
    ))

    fun createPostCopy(
        timestamp: Date,
        firebaseId: String,
        userId: String,
        authorDisplayName: String,
        authorText: MutableState<String>,
        restaurantName: MutableState<String>,
        location: GeoPoint?,
        valueMeat: MutableState<Int>,
        valueSideDishes: MutableState<Int>,
        valueAtmosphere: MutableState<Int>,
        valueAmenities: MutableState<Int>,
        photoList: List<Photo>,
        distance: Double
    ): Post {

        val newPost = Post(
            timestamp = timestamp,
            firebaseId = firebaseId,
            userId = userId,
            authorDisplayName = authorDisplayName,
            authorText = authorText,
            restaurantName = restaurantName,
            location = location,
            valueMeat = valueMeat,
            valueSideDishes = valueSideDishes,
            valueAmenities = valueAmenities,
            valueAtmosphere = valueAtmosphere,
            photoList = photoList,
            distance = 0.0
        )
        return newPost
    }

    val restaurantLat = mutableStateOf(0.0)
    val restaurantLng = mutableStateOf(0.0)
    val stateLat = mutableStateOf("")
    val stateLng = mutableStateOf("")
    val address = mutableStateOf("")
    var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    val valueMeat = mutableStateOf(0)

    val valueSideDishes = mutableStateOf(0)
    val valueAmenities = mutableStateOf(0)
    val valueAtmosphere = mutableStateOf(0)
    val authorText = mutableStateOf(0)

    fun setUser(): String {
        var user = ""
        firebaseUser?.let {
            user = it.uid
        }
        return user
    }

    fun setDisplayName(): String {
        var displayName = ""
        firebaseUser?.let {
            displayName = it.displayName.toString()
        }
        return displayName
    }

    fun setAvatar(): String {
        var avatarUrl = ""
        firebaseUser?.let {
            for (item in it.providerData) {
                when (item.providerId) {
                    "facebook.com" -> {
                        Log.d(TAG, "It's logged in with facebook")
                        avatarUrl = getFBAvatar(firebaseUser!!)
                    }
                    "google.com" -> {
                        Log.d(TAG, "It's logged in with gmail")
                        avatarUrl = it.photoUrl.toString()
                    }
                    else -> {
                        Log.d(TAG, "We failed")
                    }
                }
            }
        }
        return avatarUrl
    }

    private fun getFBAvatar(currentUser: FirebaseUser): String {
        val accessToken = AccessToken.getCurrentAccessToken()?.token
        return "${requireNotNull(currentUser.photoUrl)}?access_token=$accessToken&type=large"
    }

    private suspend fun observePosts(currentUser: FirebaseUser) {
        //mapping it into the homescreenstate
        observePosts().map { posts ->
            ProfileScreenState.Loaded(
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=987&q=80",
                posts = posts
            )
        }.collect {
            mutableState.emit(it)
        }
    }

    private fun observePosts(): Flow<List<Post>> {
        return callbackFlow {
            val userId = setUser()
            val listener = Firebase.firestore
                .collection("reviews").whereEqualTo("user_id", userId)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.d(TAG, "An error has occurred in profile screen: ${error.message}")
                        close(error)
                    } else if (value != null) {
                        val posts = value.map { documentSnapshot ->
                            val firebaseId = documentSnapshot.id
                            val userId = Firebase.auth.currentUser.toString()
                            Post(
                                timestamp = documentSnapshot.getDate("date_posted") ?: Date(),
                                userId = userId,
                                firebaseId = documentSnapshot.getString("firebase_id").orEmpty(),
                                authorDisplayName = documentSnapshot.getString("author_id")
                                    .orEmpty(),
                                authorText = mutableStateOf(
                                    documentSnapshot.getString("author_comment").orEmpty()
                                ),
                                restaurantName = mutableStateOf(
                                    documentSnapshot.getString("restaurant_name")
                                        .orEmpty()
                                ),
                                location = documentSnapshot.getGeoPoint("location"),
                                valueMeat = mutableStateOf(
                                    documentSnapshot.getLong("value_meat")!!.toInt()
                                ),
                                valueSideDishes = mutableStateOf(
                                    documentSnapshot.getLong("value_side_dishes")!!.toInt()
                                ),
                                valueAtmosphere = mutableStateOf(
                                    documentSnapshot.getLong("value_atmosphere")!!.toInt()
                                ),
                                valueAmenities = mutableStateOf(
                                    documentSnapshot.getLong("value_amenities")!!.toInt()
                                ),
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
        Log.d(
            com.example.kbbqreview.TAG,
            "Captain, we who are about to start salute you! Onward, to the photos!"
        )
        Log.d(
            com.example.kbbqreview.TAG,
            "Before we depart, we would like to check our id... ID: $firebaseId"
        )

        val photoList = mutableListOf<Photo>()
        val db = Firebase.firestore
        db.collection("photos")
            .whereEqualTo("post_id", firebaseId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d(
                        com.example.kbbqreview.TAG,
                        "Sir, we arrived at the photo, but it's empty!"
                    )
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
                Log.d(
                    com.example.kbbqreview.TAG,
                    "We regret to inform you that it has failed. Printing out failure..."
                )
                Log.d(com.example.kbbqreview.TAG, "${it.message}")
            }
        return photoList
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    fun delete(firebaseId: String) {
        val db = Firebase.firestore
        val queryPhoto = db.collection("photos").whereEqualTo("post_id", firebaseId)
        queryPhoto.get().addOnSuccessListener { result ->
            if (result.isEmpty) {
                Log.d("Delete", "Failed to get the document")
            }
            result.forEach {
                it.reference.delete()
            }
        }
        val queryReview = db.collection("reviews").whereEqualTo("firebase_id", firebaseId)
        queryReview.get().addOnSuccessListener { result ->
            if (result.isEmpty) {
                Log.d("Delete", "Failed to get the document")
            }
            result.forEach {
                it.reference.delete()
            }
        }
    }

    val editPhotoList = mutableStateListOf<Photo>()
    val photoList = mutableStateListOf<Photo>()

    fun changeLocation(latitude: Double, longitude: Double, context: Context) {
        restaurantLat.value = latitude
        restaurantLng.value = longitude
        address.value = getAddressFromLocation(context, latitude, longitude)
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

    fun updateReview(id: String, post: Post) {
        val db = Firebase.firestore.collection("reviews").document(id)
        viewModelScope.launch(Dispatchers.IO) {
            db.update("author_id", post.authorDisplayName)
            db.update("user_id", post.userId)
            db.update("date_posted", post.timestamp)
            db.update("restaurant_name", post.restaurantName)
            db.update("location", post.location)
            db.update("author_comment", post.authorText)
            db.update("value_meat", post.valueMeat)
            db.update("value_side_dishes", post.valueSideDishes)
            db.update("value_amenities", post.valueAmenities)
            db.update("value_atmosphere", post.valueAtmosphere)
        }
    }
}