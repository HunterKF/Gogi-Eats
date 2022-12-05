package com.example.gogieats.screens.login

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gogieats.data.photos.Photo
import com.example.gogieats.data.user.FullUser
import com.example.gogieats.util.LoginScreenState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class LoginViewModel : ViewModel() {
    val loadingState = MutableStateFlow<LoginScreenState>(LoginScreenState.LandingScreen)

    val state = loadingState.asStateFlow()

    fun createAccount(
        email: String,
        password: String,
        userName: String,
        profilePhoto: Photo?,
        navigateToHome: () -> Unit,
        context: Context,
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                loadingState.emit(LoginScreenState.Loading)
                Firebase.auth.createUserWithEmailAndPassword(email, password).await()
                val currentUser = Firebase.auth.currentUser
                Log.d("create account", "It started creating an account...")
                createNewAccount(currentUser, userName, profilePhoto, navigateToHome, context)
            } catch (e: Exception) {
                loadingState.emit(LoginScreenState.Error(e.localizedMessage))
            }
        }

    fun changeToCreate() = viewModelScope.launch {
        println("I AM CHANGING")
        loadingState.emit(LoginScreenState.CreateAccount)
    }

    fun changeToCreateAccCamera() = viewModelScope.launch {
        println("NO I AM CHANGING")
        loadingState.emit(LoginScreenState.CreateAccCamera)
    }

    fun changeToSettingsCamera() = viewModelScope.launch {
        println("I HAVE THREE WAYS OF CHANING??")
        loadingState.emit(LoginScreenState.ChangeSettingCamera)
    }

    fun backToLanding() = viewModelScope.launch {
        loadingState.emit(LoginScreenState.LandingScreen)
    }

    fun changeToSignIn() = viewModelScope.launch {
        loadingState.emit(LoginScreenState.SignIn)
    }

    fun simpleChangeToProfileSetting() = viewModelScope.launch {
        loadingState.emit(LoginScreenState.ChangeProfileSettings)
    }

    fun changeProfileSettings(navigateToProfile: () -> Unit) = viewModelScope.launch {
        loadingState.emit(LoginScreenState.Loading)
        val db = Firebase.firestore.collection("users")
        val currentUser = FirebaseAuth.getInstance().currentUser
        db.whereEqualTo("user_id", currentUser?.uid).get().addOnSuccessListener { result ->
            println("Checking firebase for user")
            if (result.isEmpty) {
                println("User was not found. Attempting to change state.")
                viewModelScope.launch {
                    println("State is being changed.")
                    loadingState.emit(LoginScreenState.ChangeProfileSettings)
                }
            } else {
                println("User was found, navigating to profile screen.")
                navigateToProfile()
            }
        }
    }

    fun createNewAccount(
        currentUser: FirebaseUser?,
        userName: String,
        profilePhoto: Photo?,
        navigateToHome: () -> Unit,
        context: Context,
    ) {
        val accountTag = "Account"
        Log.d(accountTag, "Create accounted has started")
        viewModelScope.launch {
            Log.d(accountTag, "1 Scope has launched")
            createFirebaseUser(currentUser, userName, context)
            Log.d(accountTag, "2 Starting photo upload")
            uploadPhotos(profilePhoto!!, currentUser!!.uid)
            Log.d(accountTag, "3 Navigating to home")
            navigateToHome()
            Log.d(accountTag, "4 All functions have been processed.")
        }
    }


    private fun createFirebaseUser(
        currentUser: FirebaseUser?,
        userName: String,
        context: Context,
    ) {
        currentUser?.let {
            val fullUser = FullUser(
                "1",
                "1",
                currentUser.uid,
                userName,
                userName.lowercase(Locale.getDefault()),
            )
            val handle = Firebase.firestore.collection("users")

            handle.document(currentUser.uid).get()
                .addOnSuccessListener { result ->
                    if (result.data == null && result.data.isNullOrEmpty()) {
                        handle.document(currentUser.uid).set(fullUser)
                    }
                    println("Successfully stored a user")
                    Toast.makeText(context, "Signed in!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    println("Failed to store user. ${it.localizedMessage}")
                    println("Failed to store user. ${it.message}")
                    Toast.makeText(context, "Failed to sign in.", Toast.LENGTH_SHORT).show()
                }
        }

    }

    fun onSignIn(context: Context) {
        val currentUser = Firebase.auth.currentUser
        viewModelScope.launch(Dispatchers.IO) {
            val db = Firebase.firestore.collection("users")
            db.whereEqualTo("user_id", currentUser?.uid).get().addOnSuccessListener { result ->
                if (result.isEmpty) {
                    createFirebaseUser(currentUser, currentUser!!.displayName!!, context)
                }
            }
        }
    }

    fun signInWithEmailAndPassword(
        context: Context,
        email: String,
        password: String,
        navigateToHome: () -> Unit,
    ) = viewModelScope.launch {
        try {
            println("Trying to login...")
            loadingState.emit(LoginScreenState.Loading)
            Firebase.auth.signInWithEmailAndPassword(email, password).await()
            Toast.makeText(context, "Signed in!", Toast.LENGTH_SHORT).show()
            navigateToHome()
        } catch (e: Exception) {
            println("Something failed... ${e.localizedMessage}")
            loadingState.emit(LoginScreenState.Error(e.localizedMessage))
            Toast.makeText(context, "Failed to sign in.", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkUserNameAvailability(
        userName: String,
        value: MutableState<Boolean>,
        context: Context,
    ): Boolean {
        viewModelScope.launch(Dispatchers.IO) {
            val db = Firebase.firestore.collection("users")
            db.whereEqualTo("user_name_lowercase", userName.lowercase()).get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        println("Nothing to report here. It was empty")
                        value.value = true
                        Toast.makeText(context, "User name available.", Toast.LENGTH_SHORT).show()
                        println("I changed the value - ${value.value}")
                    } else {
                        value.value = false
                        Toast.makeText(context, "Try another name.", Toast.LENGTH_SHORT).show()
                    }

                }.await()
        }
        return value.value
    }

    fun onTextFieldChange(textValue: MutableState<String>, query: String) {
        textValue.value = query
    }

    private fun uploadPhotos(photo: Photo, id: String) {
        val storageReference = FirebaseStorage.getInstance().reference
        viewModelScope.launch(Dispatchers.IO) {
            val uri = Uri.parse(photo.localUri)
            val imageRef =
                storageReference.child("images/${uri.lastPathSegment}")
            val uploadTask = imageRef.putFile(uri)
            uploadTask.addOnSuccessListener {
                Log.d("Firebase Image", "Image uploaded $imageRef")
                val downloadUrl = imageRef.downloadUrl
                downloadUrl.addOnSuccessListener { remoteUri ->
                    photo.remoteUri = remoteUri.toString()
                    updatePhotoData(photo, id)
                }
            }
            uploadTask.addOnFailureListener {
                Log.e("Firebase Image", it.message ?: "No message")
            }
        }
    }

    private fun updatePhotoData(photo: Photo, id: String) {
        val remoteUri = photo.remoteUri
        val localUri = photo.localUri
        viewModelScope.launch(Dispatchers.IO) {
            Firebase.firestore.collection("users").whereEqualTo("user_id", id).get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        println("Error in updating the photo")
                        return@addOnSuccessListener
                    } else {
                        result.documents.forEach {
                            it.reference.update("profile_avatar_remote_uri", remoteUri)
                            it.reference.update("profile_avatar_local_uri", localUri)
                        }
                    }
                }
        }
    }

    fun setDisplayName(): String {
        var displayName = "2"
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            displayName = it.displayName.toString()
        }
        return displayName
    }

    fun setCurrentUser(currentUser: FirebaseUser?) = viewModelScope.launch {
        if (currentUser != null) {
            loadingState.emit(LoginScreenState.SignIn)
        }
    }

    fun forgotPassword(email: String, context: Context) {
        Firebase.auth.sendPasswordResetEmail(email).addOnSuccessListener {
            println("Email was sent!")
        }.addOnFailureListener {
            println("A problem occurred: ${it.localizedMessage}")
            Toast.makeText(context,
                "An error has occurred: ${it.localizedMessage}",
                Toast.LENGTH_SHORT).show()
        }
    }


}

