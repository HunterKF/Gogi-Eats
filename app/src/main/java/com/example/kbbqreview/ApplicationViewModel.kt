package com.example.kbbqreview

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.kbbqreview.data.user.User
import com.example.kbbqreview.screens.map.location.LocationLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.flow.MutableStateFlow

class ApplicationViewModel(application: Application) : AndroidViewModel(application) {
    private val locationLiveData = LocationLiveData(application)


    private lateinit var firestore: FirebaseFirestore

    internal val NEW_NAME = "New restaurant"
    var user: User? = null
    var currentUser by mutableStateOf<FirebaseUser?>(null)
    var liveDateUser = MutableLiveData<FirebaseUser?>()

    @JvmName("assignCurrentUser")
    fun setCurrentUser(user: FirebaseUser?) {
        currentUser = user
        liveDateUser.value = user
    }
    val activeUser = mutableStateOf(user)

    var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser


    private val _isRefreshing = MutableStateFlow(false)

    init {


        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
//        listenToReviews()
    }

    //Location functions
    fun getLocationLiveData() = locationLiveData

    fun startLocationUpdates() {
        locationLiveData.startLocationUpdates()
    }


    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        user = null
        activeUser.value = null
    }

    private fun saveUser(user: User) {
        val handle = firestore.collection("users").document(user.uid).set(user)
        activeUser.value = user
        handle.addOnSuccessListener { Log.d("Firebase", "Document saved") }
        handle.addOnFailureListener { Log.e("Firebase", "Saved failed $it") }
    }



}