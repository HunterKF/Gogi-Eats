package com.example.kbbqreview

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.kbbqreview.data.photos.Photo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.*


val TAG = "HomeScreenVM"
class HomeScreenViewModel : ViewModel() {


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
}