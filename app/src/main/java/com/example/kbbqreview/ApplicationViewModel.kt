package com.example.kbbqreview

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.data.roomplaces.StoredPlace
import com.example.kbbqreview.data.roomplaces.StoredPlaceDatabase
import com.example.kbbqreview.data.roomplaces.StoredPlaceRepository
import com.example.kbbqreview.data.user.User
import com.example.kbbqreview.screens.map.location.LocationLiveData
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class ApplicationViewModel(application: Application) : AndroidViewModel(application) {
    private val locationLiveData = LocationLiveData(application)

    var readAllData: LiveData<List<StoredPlace>>
    private var repository: StoredPlaceRepository

    private val searchResults: MutableLiveData<List<StoredPlace>>

    private lateinit var firestore: FirebaseFirestore
    private var storageReference = FirebaseStorage.getInstance().getReference()

    internal val NEW_NAME = "New restaurant"
    var user: User? = null
    var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    var reviews: MutableLiveData<List<StoredPlace>> = MutableLiveData<List<StoredPlace>>()
    val eventPhotos: MutableLiveData<List<Photo>> = MutableLiveData<List<Photo>>()


    init {
        val storedPlaceDatabase = StoredPlaceDatabase.getDatabase(application)
        val storedPlaceDao = storedPlaceDatabase.userDao()
        repository = StoredPlaceRepository(storedPlaceDao)

        readAllData = repository.readAllData
        searchResults = repository.searchResults

        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        listenToReviews()
    }

    //Location functions
    fun getLocationLiveData() = locationLiveData

    fun startLocationUpdates() {
        locationLiveData.startLocationUpdates()
    }

    fun listenToReviews() {
        //this fetches the reviews
        user?.let { user ->
            firestore.collection("users").document(user.uid).collection("reviews")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("Listen failed", e)
                        return@addSnapshotListener
                    }
                    snapshot?.let {
                        val allReviews = ArrayList<StoredPlace>()
                        allReviews.add(StoredPlace(name = NEW_NAME))
                        val documents = snapshot.documents
                        documents.forEach {
                            firestore.collection("users").document(user.uid).collection("reviews")
                                .document(it.id).collection("photos")
                                .addSnapshotListener { snapshot, e ->
                                    if (e != null) {
                                        Log.w("Listen failed", e)
                                        return@addSnapshotListener
                                    }
                                    snapshot?.let {
                                        val inPhotos = ArrayList<Photo>()
                                        inPhotos.add(Photo())
                                        val photoDocument = snapshot.documents
                                        photoDocument.forEach {
                                            val photo = it.toObject(Photo::class.java)
                                            photo?.let {
                                                inPhotos.add(it)
                                            }
                                        }
                                        eventPhotos.value = inPhotos
                                    }
                                }
                            val review = it.toObject(StoredPlace::class.java)
                            review?.let {
                                allReviews.add(it)
                            }
                        }
                        reviews.value = allReviews
                    }
                }
        }
    }

    fun listenToPhotos() {
        //this fetches the photos
        user?.let { user ->
            firestore.collection("users").document(user.uid).collection("reviews")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("Listen failed", e)
                        return@addSnapshotListener
                    }
                    snapshot?.let {
                        val allPhotos = ArrayList<Photo>()
                        allPhotos.add(Photo())
                        val documents = snapshot.documents
                        documents.forEach {
                            val review = it.toObject(Photo::class.java)
                            review?.let { photo ->
                                allPhotos.add(photo)
                            }
                        }
                        eventPhotos.value = allPhotos
                    }
                }
        }
    }


    fun signOn(signInLauncher: ManagedActivityResultLauncher<Intent, FirebaseAuthUIAuthenticationResult>) {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )
        val signinIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(
                providers
            )
            .build()
        signInLauncher.launch(signinIntent)
    }


    fun signInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == Activity.RESULT_OK) {
            this.firebaseUser = FirebaseAuth.getInstance().currentUser
            firebaseUser?.let {
                val user = User(it.uid, it.displayName!!)
                saveUser(user)
            }
        } else {
            Log.e("Authentication", "Error logging in" + response?.error?.errorCode)
        }
    }

    private fun saveUser(user: User) {
        val handle = firestore.collection("users").document(user.uid).set(user)

        handle.addOnSuccessListener { Log.d("Firebase", "Document saved") }
        handle.addOnFailureListener { Log.e("Firebase", "Saved failed $it") }
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


    fun saveReview(storedPlace: StoredPlace, selectImages: SnapshotStateList<Photo>) {
        user?.let { user ->
            val document =
                if (storedPlace.firebaseId == null || storedPlace.firebaseId.isEmpty()) {
                    //create new review
                    firestore.collection("users").document(user.uid).collection("reviews")
                        .document()
                } else {
                    //update review
                    firestore.collection("reviews").document(user.uid).collection("reviews")
                        .document(storedPlace.firebaseId)
                }
            storedPlace.firebaseId = document.id
            val handle = document.set(storedPlace)
            handle.addOnSuccessListener {
                Log.d("Firebase", "Document saved")
                if (selectImages.isNotEmpty()) {
                    uploadPhotos(selectImages, storedPlace)
                }
            }
            handle.addOnFailureListener { Log.e("Firebase", "Saved failed $it") }
        }

    }

    fun uploadPhotos(selectImages: SnapshotStateList<Photo>, storedPlace: StoredPlace) {
        selectImages.forEach { photo ->
            var uri = Uri.parse(photo.localUri)
            var imageRef =
                storageReference.child("images/${user?.uid}/${uri.lastPathSegment}")
            val uploadTask = imageRef.putFile(uri)
            uploadTask.addOnSuccessListener {
                Log.d("Firebase Image", "Image uploaded $imageRef")
                val downloadUrl = imageRef.downloadUrl
                downloadUrl.addOnSuccessListener { remoteUri ->
                    photo.remoteUri = remoteUri.toString()
                    updatePhotoData(photo, storedPlace = storedPlace)
                }
            }
            uploadTask.addOnFailureListener {
                Log.e("Firebase Image", it.message ?: "No message")
            }
        }
    }

    private fun updatePhotoData(photo: Photo, storedPlace: StoredPlace) {
        user?.let { user ->
            val photoCollection =
                firestore.collection("users").document(user.uid).collection("reviews")
                    .document(storedPlace.firebaseId).collection("photos")
            val handle = photoCollection.add(photo)
            handle.addOnSuccessListener {
                Log.d("Firebase Image", "Successfully updated photo metadata")
                photo.id = it.id
                firestore.collection("users").document(user.uid).collection("reviews")
                    .document(storedPlace.firebaseId).collection("photos").document(photo.id)
                    .set(photo)
            }
            handle.addOnFailureListener {
                Log.e("Firebase Image", "Error updating photo data: ${it.message}")
            }
        }

    }

    fun fetchPhotos(storedPlace: StoredPlace) {
        user?.let { user ->
            val photoCollection =
                firestore.collection("users").document(user.uid).collection("reviews")
                    .document(storedPlace.firebaseId).collection("photos")
            val photoListener =
                photoCollection.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    querySnapshot?.let { querySnapshot ->
                        val documents = querySnapshot.documents
                        var inPhotos = ArrayList<Photo>()
                        documents?.forEach {
                            val photo = it.toObject(Photo::class.java)
                            photo?.let { photo ->
                                inPhotos.add(photo)
                            }
                        }
                        eventPhotos.value = inPhotos
                    }
                }
        }
    }

    fun fetchReview(selectImages: SnapshotStateList<Photo>, storedPlace: StoredPlace) {
        selectImages.clear()
        user?.let { user ->
            val photoCollection =
                firestore.collection("users").document(user.uid).collection("reviews")
                    .document(storedPlace.firebaseId).collection("photos")
            val photoListener =
                photoCollection.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    querySnapshot?.let { querySnapshot ->
                        val documents = querySnapshot.documents
                        documents?.forEach {
                            val photo = it.toObject(Photo::class.java)
                            photo?.let { photo ->
                                selectImages.add(photo)
                            }
                        }
                    }
                }
        }
    }

    fun compressImage(context: ComponentActivity, photo: Photo): Uri? {
        val bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                photo.localUri.toUri()
            )
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, photo.localUri.toUri())
            ImageDecoder.decodeBitmap(source)
        }
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bytes)
        val path: String = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            bitmap,
            "Title",
            null
        )
        return Uri.parse(path)
    }

    suspend fun uploadPhoto(
        uri: Uri,
        name: String,
        mimeType: String?,
        callback: (url: String) -> Unit
    ) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val fileRef = storageRef.child("images/$name")

        val metadata = mimeType?.let {
            StorageMetadata.Builder()
                .setContentType(mimeType)
                .build()
        }
        if (metadata != null) {
            fileRef.putFile(uri, metadata).await()
        } else {
            fileRef.putFile(uri).await()
        }

        callback(fileRef.downloadUrl.await().toString())
    }


}