package com.example.kbbqreview

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ApplicationViewModel(application: Application) : AndroidViewModel(application) {
    private val locationLiveData = LocationLiveData(application)

    var readAllData: LiveData<List<StoredPlace>>
    private var repository: StoredPlaceRepository

    private val searchResults: MutableLiveData<List<StoredPlace>>

    private lateinit var firestore: FirebaseFirestore
    private var storageReference = FirebaseStorage.getInstance().getReference()

    internal val NEW_NAME = "New restaurant"
    var user: User? = null
    val activeUser = mutableStateOf(user)

    var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    val fetchedPhotos = ArrayList<Photo>()
    val eventPhotos: MutableLiveData<List<Photo>> = MutableLiveData<List<Photo>>()
    val userList: MutableLiveData<List<User>> = MutableLiveData<List<User>>()

    val listOfStoryItem = mutableListOf<StoryItem>()
    val storyFeed = MutableLiveData<StoryItemList>()
    val listOfUserReviews = mutableListOf<StoryItem>()
    val userReviews = MutableLiveData<StoryItemList>()
    private val _isRefreshing = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    fun refresh() {
        // This doesn't handle multiple 'refreshing' tasks, don't use this

        viewModelScope.launch {
            // A fake 2 second 'refresh'
            _isRefreshing.emit(true)
            listenToAllUsers()
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

    /*fun signOn(signInLauncher: ManagedActivityResultLauncher<Intent, FirebaseAuthUIAuthenticationResult>) {
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
    }*/

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


    fun saveReview(
        storedPlace: StoredPlace,
        selectImages: SnapshotStateList<Photo>
    ) {
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

    private fun uploadPhotos(
        selectImages: SnapshotStateList<Photo>,
        storedPlace: StoredPlace
    ) {
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
        selectImages.clear()
    }

    private fun updatePhotoData(photo: Photo, storedPlace: StoredPlace) {
        user?.let { user ->
            val photoCollection =
                firestore.collection("users").document(user.uid).collection("reviews")
                    .document(storedPlace.firebaseId).collection("photos")
            val handle = photoCollection.add(photo)
            handle.addOnSuccessListener {
                Log.d("Firebase Image", "Successfully updated photo metadata")
                photo.firebaseId = it.id
                firestore.collection("users").document(user.uid).collection("reviews")
                    .document(storedPlace.firebaseId).collection("photos").document(photo.firebaseId)
                    .set(photo)
            }
            handle.addOnFailureListener {
                Log.e("Firebase Image", "Error updating photo data: ${it.message}")
            }
        }

    }


    /*fun compressImage(context: ComponentActivity, photo: Photo): Uri? {
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
    }*/

    fun listenToUserReview() {
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
                        val inPhotos = ArrayList<Photo>()
                        val documents = snapshot.documents
                        documents.forEach {
                            val review = it.toObject(StoredPlace::class.java)
                            review?.let {
                                allReviews.add(it)
                            }
                            firestore.collection("users").document(user.uid).collection("reviews")
                                .document(it.id).collection("photos")
                                .addSnapshotListener { snapshot, e ->
                                    if (e != null) {
                                        Log.w("Listen failed", e)
                                        return@addSnapshotListener
                                    }
                                    snapshot?.let {
                                        val photoDocument = snapshot.documents
                                        photoDocument.forEach {
                                            val photo = it.toObject(Photo::class.java)
                                            photo?.let {
                                                inPhotos.add(it)
                                            }
                                        }
                                        listOfUserReviews.add(StoryItem(review!!, inPhotos))
                                        eventPhotos.value = inPhotos
                                    }
                                }
                        }
                        userReviews.value = StoryItemList(listOfUserReviews)
                    }
                }
        }
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
                        handle.document(user.uid).collection("reviews").orderBy("date", Query.Direction.DESCENDING)
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
                                        val photoList = getPhotos(handle, user, review!!)
                                        if (!_isRefreshing.value) {
                                            //this is being called...? DEBUG DEBUG DEBUG
                                            if (storyFeed.value?.storyList?.contains(StoryItem(review, photoList)) == true) {
                                                //pass
                                                Log.d("Submit", "The value is already in. Updating has been skipped.")
                                            } else {
                                                Log.d("Submit", "A new review was detected. Adding new review.")
                                                if (listOfStoryItem.contains(StoryItem(review, photoList))){
                                                    println("SCREAM AGAIN AND AGAIN TILL YOU'RE HAPPY")
                                                    println("SCREAM AGAIN AND AGAIN TILL YOU'RE HAPPY")
                                                    println("SCREAM AGAIN AND AGAIN TILL YOU'RE HAPPY")
                                                }
                                                listOfStoryItem.add(StoryItem(review, photoList))
                                            }

                                        }
                                        //Hypothetically, I don't need a function to update the story feed
//                                        updateStoryFeed()
//                                        storyFeed.value = StoryItemList(listOfStoryItem)
                                        //THIS IS BEING CALLLED DEBUG DEBUG
                                        review.let {
                                            Log.d("listenToUsers", "Review has been stored.")
                                            storedPlace = it
                                        }
                                    }
                                }
                            }

                    }
                }
                userList.value = listOfUsers
                if (storyFeed.value != null) {
                    storyFeed.value!!.storyList.clear()
                    storyFeed.value = StoryItemList(listOfStoryItem)
                }
                storyFeed.value = StoryItemList(listOfStoryItem)
                listOfStoryItem.clear()
            }
        }

    }

    //Working on adding the two lists together. Right now, it is just adding the same list twice, so the result is a list with the same thing being added.
    //it is not filtering out anything right now.
    private fun updateStoryFeed() {
        if (_isRefreshing.value) {
            Log.d("Submit", "Submit has triggered this function.")
            val incomingList = StoryItemList(listOfStoryItem)
            val originalList = storyFeed.value
            originalList?.let {
                val differences = originalList.storyList.minus(incomingList.storyList)
                Log.d("Update story", "The size of differences is: ${differences.size}")
                if (differences.isNotEmpty()) {
                    differences.let {
                        incomingList.storyList.clear()
                        it.forEach {
                            Log.d(
                                "Update story",
                                "Before story is updated. Current size of story feed is: ${storyFeed.value?.storyList?.size}"
                            )
                            storyFeed.value!!.storyList.add(it)
                            Log.d(
                                "Update story",
                                "Story has been updated. Current size of story feed is: ${storyFeed.value?.storyList?.size}"
                            )
                        }
                    }
                }
            }

        } else {
            storyFeed.value = StoryItemList(listOfStoryItem)
        }
    }


    private fun getPhotos(
        handle: CollectionReference,
        user: User,
        review: StoredPlace
    ): ArrayList<Photo> {

        Log.d("listenToUsers", "Photos has started firing.")
        var photoList = ArrayList<Photo>()
        val photoHandle =
            handle.document(user.uid).collection("reviews").document(review.firebaseId)
                .collection("photos").orderBy("listIndex", Query.Direction.ASCENDING)
        photoHandle.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Photo listen failed", e)
                return@addSnapshotListener
            }
            snapshot?.let {
                val photoDocument = snapshot.documents
                photoDocument.forEach {
                    Log.d("listenToUsers", "Photos for each has fired.")
                    val photo = it.toObject(Photo::class.java)
                    photoList.add(photo!!)
                    photo.let {
                        fetchedPhotos.add(it)
                        Log.d("listenToUsers", "The value for user.id is : ${review.name}")
                    }
                }
            }
        }

        return photoList
    }

    fun getReviews() {
         val db = FirebaseFirestore.getInstance()
    }
}