package com.example.kbbqreview.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kbbqreview.Post
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.data.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
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
    private val mutableState = MutableStateFlow<ProfileScreenState>(
        ProfileScreenState.Loading
    )
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {

            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {
                Log.d(TAG, "If statement has been triggered.")
                observePosts(currentUser)
            } else {
                Log.d(TAG, "Else statement has been triggered.")
                mutableState.emit(
                    ProfileScreenState.SignInRequired
                )
            }
        }
    }

    var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    fun setUser(): String {
        var user = ""
        firebaseUser?.let {
            user = it.uid
        }
        return user
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
                                authorDisplayName = documentSnapshot.getString("author_id")
                                    .orEmpty(),
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
    /* private fun getAvatar(currentUser: FirebaseUser): String {
         val accessToken = AccessToken.getCurrentAccessToken()?.token
         return "${requireNotNull(currentUser.photoUrl)}?access_token=$accessToken&type=large"
     }*/

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
}