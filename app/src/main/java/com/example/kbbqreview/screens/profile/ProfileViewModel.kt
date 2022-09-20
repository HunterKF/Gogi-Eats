package com.example.kbbqreview.screens.profile

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kbbqreview.data.firestore.EditingPost
import com.example.kbbqreview.data.firestore.Post
import com.example.kbbqreview.data.photos.Photo
import com.facebook.AccessToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
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


    val restaurantName = mutableStateOf("")
    val editingState = mutableStateOf(false)
    private val mutableState = MutableStateFlow<ProfileScreenState>(
        ProfileScreenState.Loading
    )
    val newUser = mutableStateOf(FirebaseAuth.getInstance().currentUser != null)

    val state = mutableState.asStateFlow()
    var currentUser = Firebase.auth.currentUser
    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (currentUser != null) {
                observePosts(currentUser!!)
                println("The current user is signed in.")
            } else {
                mutableState.emit(
                    ProfileScreenState.SignInRequired
                )
            }
        }
    }

    var post = mutableStateOf(
        Post(
            timestamp = Date(),
            firebaseId = "",
            userId = "",
            authorDisplayName = "",
            authorText = "",
            restaurantName = "",
            location = GeoPoint(0.0, 0.0),
            valueMeat = 0,
            valueSideDishes = 0,
            valueAtmosphere = 0,
            valueAmenities = 0,
            photoList = listOf(),
            distance = 0.0
        )
    )
    var editingPost = EditingPost(
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
    )

    fun convertPostToEditingPost(
        post: Post,
    ): EditingPost {
        return EditingPost(
            timestamp = post.timestamp,
            firebaseId = post.firebaseId,
            userId = post.userId,
            authorDisplayName = post.authorDisplayName,
            authorText = mutableStateOf(post.authorText),
            restaurantName = mutableStateOf(post.restaurantName),
            location = post.location,
            valueMeat = mutableStateOf(post.valueMeat),
            valueSideDishes = mutableStateOf(post.valueSideDishes),
            valueAmenities = mutableStateOf(post.valueAmenities),
            valueAtmosphere = mutableStateOf(post.valueAtmosphere),
            photoList = post.photoList,
            distance = 0.0
        )
    }

    fun converEditingPostToPost(
        editingPost: EditingPost,
    ): Post {
        return Post(
            timestamp = editingPost.timestamp,
            firebaseId = editingPost.firebaseId,
            userId = editingPost.userId,
            authorDisplayName = editingPost.authorDisplayName,
            authorText = editingPost.authorText.value,
            restaurantName = editingPost.restaurantName.value,
            location = editingPost.location,
            valueMeat = editingPost.valueMeat.value,
            valueSideDishes = editingPost.valueSideDishes.value,
            valueAmenities = editingPost.valueAmenities.value,
            valueAtmosphere = editingPost.valueAtmosphere.value,
            photoList = editingPost.photoList,
            distance = 0.0
        )
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
                    photoList.sortBy { it.listIndex }
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

    fun delete(firebaseId: String, post: Post) {
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
        deleteStoragePhoto(post)
    }

    private fun deleteStoragePhoto(post: Post) {
        post.photoList.forEach { photo ->
            deleteSinglePhoto(photo)
        }
    }

    val editPhotoList = mutableStateListOf<Photo>()
    val toBeDeletedPhotoList = mutableStateListOf<Photo>()
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

    fun updateReview(
        firebaseId: String,
        post: EditingPost,
        editPhotoList: SnapshotStateList<Photo>,
    ) {
        val convertedPost = converEditingPostToPost(post)
        val db = Firebase.firestore

        val queryReview = db.collection("reviews").document(firebaseId)
        viewModelScope.launch(Dispatchers.IO) {
            queryReview.update("restaurant_name", post.restaurantName.value)
            queryReview.update("location", post.location)
            queryReview.update("author_comment", post.authorText.value)
            queryReview.update("value_meat", post.valueMeat.value)
            queryReview.update("value_side_dishes", post.valueSideDishes.value)
            queryReview.update("value_amenities", post.valueAmenities.value)
            queryReview.update("value_atmosphere", post.valueAtmosphere.value)
            updatesPhotos(convertedPost, editPhotoList)
        }
        if (toBeDeletedPhotoList.isNotEmpty()) {
            toBeDeletedPhotoList.forEach { photo ->
                val queryPhoto = db.collection("photos").whereEqualTo("remote_uri", photo.remoteUri)

                deleteSinglePhoto(photo)

                queryPhoto.get().addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        Log.d("Delete", "Failed to get the document")
                    }
                    result.forEach {
                        it.reference.delete()
                    }
                }
            }
        }
    }

    private fun deleteSinglePhoto(photo: Photo) {
        val storage = Firebase.storage
        val storageReference = storage.reference
        val uri = Uri.parse(photo.localUri)
        val imageRef =
            storageReference.child("images/${uri.lastPathSegment}")
        imageRef.delete()
    }

    private fun updatesPhotos(post: Post, editPhotoList: SnapshotStateList<Photo>) {
        Log.d(TAG, "updatePhotos() has been started")
        val db = Firebase.firestore.collection("photos")
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "Starting to update photos.")
            Log.d(TAG, "Photo list size: ${photoList.size}")
            post.photoList.forEach { photo ->
                db.whereEqualTo("local_uri", photo.localUri).get().addOnSuccessListener { result ->
                    if (result == null || result.isEmpty) {
                        Log.d(TAG, "The result is empty. Going to uploadPhotos()")
                        uploadPhotos(photo, post.firebaseId)
                    } else {
                        result.forEach {
                            Log.d(TAG, "The result was not empty. Going to updateIndex()")
                            updateIndex(photo)
                        }
                    }
                }
            }

        }
    }


    private fun updateIndex(photo: Photo) {
        val db = Firebase.firestore.collection("photos").whereEqualTo("remote_uri", photo.remoteUri)
        viewModelScope.launch(Dispatchers.IO) {
            db.get().addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d(TAG, "Could not update the index. Try again later.")
                    return@addOnSuccessListener
                } else {
                    result.documents.forEach {
                        Log.d(TAG, "Attempting to update the index inside the doc.forEach {} loop")
                        it.reference.update("list_index", photo.listIndex)
                    }
                }
            }
        }

    }

    private fun uploadPhotos(photo: Photo, id: String) {
        val storageReference = FirebaseStorage.getInstance().reference
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "Attempting to upload a photo.")
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


    private fun updatePhotoData(photo: Photo, id: String) {
        val localUri = photo.localUri
        val remoteUri = photo.remoteUri
        val listIndex = photo.listIndex
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "Updating the metadata of the photo.")
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

    fun addPhotoToBeDeleted(photo: Photo) {
        toBeDeletedPhotoList.add(photo)
        Log.d(TAG, "A photo was queued for deletion. Current size: ${toBeDeletedPhotoList.size}")
    }

    @JvmName("assignCurrentUserProfileVM")
    fun setCurrentUser(user: FirebaseUser?) {
        Log.d("ProfileVM", "Incoming user details: ${user?.uid}")
        currentUser = user
        Log.d("ProfileVM", "The user has been changed. ${currentUser?.uid}")
    }
}