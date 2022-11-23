package com.example.kbbqreview.util

import android.util.Log
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.data.user.FullUser
import com.example.kbbqreview.screens.profile.TAG
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class HandleUser {
    companion object {
        val db = Firebase.firestore
        suspend fun getUser(uid: String): String {
            var fullUser = FullUser()
            db.collection("users").whereEqualTo("user_id", uid).get()
                .addOnSuccessListener { result ->
                    Log.d("handleUser", "Attempting to create full user: ${result}")

                    result?.forEach {
                        val newUser = it.toObject(FullUser::class.java)
                        Log.d("handleUser", "Attempting to create full user: ${newUser}")
                        if (newUser != null) {
                            fullUser = newUser
                        }
                    }
                }.await()

            Log.d("handleUser", "returning user: ${fullUser.profile_avatar_remote_uri}")
            return fullUser.profile_avatar_remote_uri
        }
    }
}