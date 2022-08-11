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
import androidx.compose.runtime.mutableStateOf
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
import com.example.kbbqreview.data.storyfeed.StoryItem
import com.example.kbbqreview.data.storyfeed.StoryItemList
import com.example.kbbqreview.data.user.User
import com.example.kbbqreview.screens.map.location.LocationLiveData
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    val fetchedPhotos = ArrayList<Photo>()
    val eventPhotos: MutableLiveData<List<Photo>> = MutableLiveData<List<Photo>>()
    val userList: MutableLiveData<List<User>> = MutableLiveData<List<User>>()

    val listOfStoryItem = mutableListOf<StoryItem>()
    val storyFeed = MutableLiveData<StoryItemList>()
    private val _isRefreshing = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()
    fun refresh() {
        // This doesn't handle multiple 'refreshing' tasks, don't use this

        viewModelScope.launch {
            // A fake 2 second 'refresh'
            _isRefreshing.emit(true)
            delay(2000)
            _isRefreshing.emit(false)
        }
    }

    init {
        val storedPlaceDatabase = StoredPlaceDatabase.getDatabase(application)
        val storedPlaceDao = storedPlaceDatabase.userDao()
        repository = StoredPlaceRepository(storedPlaceDao)

        readAllData = repository.readAllData
        searchResults = repository.searchResults

        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
//        listenToReviews()
    }

    //Location functions
    fun getLocationLiveData() = locationLiveData

    fun startLocationUpdates() {
        locationLiveData.startLocationUpdates()
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

        var indexInt = 0
        selectImages.forEach { photo ->
            photo.listIndex = indexInt
            indexInt += 1
            Log.d("Firebase Image", "Image index: ${photo.listIndex}")
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


//This is the good fun fun
    fun listenToAllUsers() {
        val handle = firestore.collection("users")
        handle.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("User listen failed", e)
                return@addSnapshotListener
            }
            //Snapshot for users
            snapshot?.let {
                var listOfUsers = ArrayList<User>()
                var storedPlace: StoredPlace
                val listOfPhotos = ArrayList<Photo>()
                var storyItemList: StoryItemList
                val users = snapshot.documents
                users.forEach {
                    Log.d("listenToUsers", "Users has fired.")
                    var user = it.toObject(User::class.java)
                    user?.let {
                        listOfUsers.add(user)
                    }
                    user?.let { user ->
                        handle.document(user.uid).collection("reviews")
                            .addSnapshotListener { snapshot, e ->

                                Log.d("listenToUsers", "Reviews has started firing.")
                                if (e != null) {
                                    Log.w("Review listen failed", e)
                                    return@addSnapshotListener
                                }
                                snapshot?.let {
                                    var reviews = snapshot.documents
                                    reviews.forEach {
                                        Log.d("listenToUsers", "Reviews has fired.")
                                        var review = it.toObject(StoredPlace::class.java)
                                        getPhotos(handle, user, review!!)
                                        review?.let {
                                            Log.d("listenToUsers", "Review has been stored.")
                                            storedPlace = it
                                        }
                                    }
                                }
                            }

                    }
                }
                userList.value = listOfUsers
            }
        }

    }

    private fun updateStoryFeed() {
        storyFeed.value = StoryItemList(listOfStoryItem)
    }


    private fun getPhotos(handle: CollectionReference, user: User, review: StoredPlace) {

        Log.d("listenToUsers", "Photos has started firing.")
        val photoHandle = handle.document(user.uid).collection("reviews").document(review.firebaseId).collection("photos").orderBy("listIndex", Query.Direction.ASCENDING)
            photoHandle.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Photo listen failed", e)
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val photoDocument = snapshot.documents
                    var photoList = ArrayList<Photo>()
                    photoDocument.forEach {
                        Log.d("listenToUsers", "Photos for each has fired.")
                        val photo = it.toObject(Photo::class.java)
                        photoList.add(photo!!)
                        photo?.let {
                            fetchedPhotos.add(it)
                            Log.d("listenToUsers", "The value for it.dataTaken is : ${it.dateTaken}")
                            Log.d("listenToUsers", "The value for user.id is : ${review.name}")
                        }
                    }

                    listOfStoryItem.add(StoryItem(review, photoList))

                    updateStoryFeed()
                    eventPhotos.value = fetchedPhotos
                    Log.d("listenToUsers", "The value for storyFeed is : ${storyFeed.value}")
                }
            }
    }

}