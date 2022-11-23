package com.example.kbbqreview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kbbqreview.data.firestore.Post
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.HomeScreen.HomeScreenState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*


val TAG = "HomeScreenVM"

class HomeScreenViewModel : ViewModel() {
    private val loadingState = MutableStateFlow<HomeScreenState>(HomeScreenState.Loading)

    val state = loadingState.asStateFlow()

    fun getPosts(){
        viewModelScope.launch(Dispatchers.IO) {
            startObersevePosts()
        }
    }
    private suspend fun startObersevePosts() {
        //mapping it into the homescreenstate

        observePosts().map { posts ->
            HomeScreenState.Loaded(
                posts = posts
            )
        }.collect {
            Thread.sleep(1000)
            loadingState.emit(it)
            println("Emitting state! State is changing!!")
        }
    }
    private fun observePosts(): Flow<List<Post>> {
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
                                userId = documentSnapshot.getString("user_id").orEmpty(),
                                firebaseId = documentSnapshot.getString("firebase_id").orEmpty(),
                                authorDisplayName = documentSnapshot.getString("author_id")
                                    .orEmpty(),
                                authorText = documentSnapshot.getString("author_comment").orEmpty(),
                                restaurantName = documentSnapshot.getString("restaurant_name")
                                    .orEmpty(),
                                location = documentSnapshot.getGeoPoint("location"),
                                valueMeat = documentSnapshot.getLong("value_meat")!!.toInt(),
                                valueSideDishes = documentSnapshot.getLong("value_side_dishes")!!.toInt(),
                                valueAtmosphere = documentSnapshot.getLong("value_atmosphere")!!.toInt(),
                                valueAmenities = documentSnapshot.getLong("value_amenities")!!.toInt(),
                                photoList = getPhotos(firebaseId),
                                distance = 0.0
                            )
                        }.sortedByDescending { it.timestamp }
                        trySend(posts)
                    }
                }
            awaitClose {
                listener.remove()
            }
        }
    }
    private fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap): Uri? {
        val path: String = context.externalCacheDir.toString() + "/testImg.jpg"
        var out: OutputStream? = null
        val file = File(path)
        try {
            out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return FileProvider.getUriForFile(
            context, context.packageName + ".com.vips.jetcapture.provider", file
        )
    }
    fun shareImageToOthers(context: Context, text: String?, bitmap: Bitmap?) {
        val imageUri: Uri? = bitmap?.let { saveBitmapAndGetUri(context, it) }
        val chooserIntent = Intent(Intent.ACTION_SEND)
        chooserIntent.type = "image/*"
        chooserIntent.putExtra(Intent.EXTRA_TEXT, text)
        chooserIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
        chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(chooserIntent)
        } catch (ex: Exception) {
        }
    }

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
                    photoList.sortBy{ it.listIndex }
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "We regret to inform you that it has failed. Printing out failure...")
                Log.d(TAG, "${it.message}")
            }
        return photoList
    }

    fun sendMail(to: String, subject: String): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "vnd.android.cursor.item/email"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        return intent
    }

}