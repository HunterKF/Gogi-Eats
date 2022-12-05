package com.example.gogieats.util

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.example.gogieats.data.user.FullUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserViewModel {
    val user = mutableStateOf(FullUser())

    val db = Firebase.firestore
    fun getUser(uid: String) {
        Log.d("handleUser", "Starting getUser: ${uid}")
        db.collection("users").document(uid).get()
            .addOnSuccessListener { result ->
                Log.d("handleUser", "Attempting to create full user: ${result}")
                if (result != null) {
                    Log.d("handleUser", "Result was not null: ${result}")

                    val newUser = result.toObject(FullUser::class.java)
                    Log.d("handleUser", "User has been created: ${newUser}")
                    newUser?.let {
                        user.value = it
                    }
                }

            }
            .addOnFailureListener{
                Log.d("handleUser", it.localizedMessage)
            }
    }
}
