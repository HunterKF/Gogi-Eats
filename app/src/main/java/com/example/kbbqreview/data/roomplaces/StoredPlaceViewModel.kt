package com.example.kbbqreview.data.roomplaces

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.kbbqreview.data.photos.Photo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StoredPlaceViewModel(application: Application) : AndroidViewModel(application) {

    var readAllData: LiveData<List<StoredPlace>>
    private var repository: StoredPlaceRepository

    private val searchResults: MutableLiveData<List<StoredPlace>>

    private lateinit var firestore: FirebaseFirestore
    private var storageReference = FirebaseStorage.getInstance().getReference()
    val user : User? = null

    var photoList: ArrayList<Photo> =  ArrayList<Photo>()

    init {
        val storedPlaceDatabase = StoredPlaceDatabase.getDatabase(application)
        val storedPlaceDao = storedPlaceDatabase.userDao()
        repository = StoredPlaceRepository(storedPlaceDao)

        readAllData = repository.readAllData
        searchResults = repository.searchResults

        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
    }

    fun addStoredPlace(storedPlace: StoredPlace) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addStoredItem(storedPlace)
        }
    }

    fun deleteStoredItem(storedPlace: StoredPlace) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteStoredItem(storedPlace)
        }
    }

    fun searchStoredItem(id: Long) {
        repository.searchStoredItem(id)
    }



    fun save(storedPlace: StoredPlace) {
        val document =
            if (storedPlace.firebaseId == null || storedPlace.firebaseId.isEmpty()) {
                firestore.collection(
                    "reviews"
                ).document()
            } else {
                firestore.collection("reviews").document(storedPlace.firebaseId)
            }
        storedPlace.firebaseId = document.id
        val handle = document.set(storedPlace)
        handle.addOnSuccessListener { Log.d("Firebase", "Document saved") }
        handle.addOnFailureListener { Log.d("Firebase", "Saved failed $it") }
    }
    fun uploadPhotos() {
        photoList.forEach { photo ->
            val uri = Uri.parse(photo.localUri)
            val imageRef = storageReference.child("images/${user?.uid}/${uri.lastPathSegment}")

        }
    }



}