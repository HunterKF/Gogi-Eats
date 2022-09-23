package com.example.kbbqreview.screens.login

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.util.LoginScreenState
import com.firebase.ui.auth.R
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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

class LoginViewModel : ViewModel() {
    val loadingState = MutableStateFlow<LoginScreenState>(LoginScreenState.LandingScreen)

    val state = loadingState.asStateFlow()

    fun createAccount(
        email: String,
        password: String,
        userName: String,
        profilePhoto: Photo?,
        navigateToHome: () -> Unit,
        context: Context
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                loadingState.emit(LoginScreenState.Loading)
                Firebase.auth.createUserWithEmailAndPassword(email, password).await()
                val currentUser = Firebase.auth.currentUser
                createFirestoreUser(currentUser, userName, profilePhoto, navigateToHome, context)
            } catch (e: Exception) {
                loadingState.emit(LoginScreenState.Error(e.localizedMessage))
            }
        }

    fun changeToCreate() = viewModelScope.launch {
        loadingState.emit(LoginScreenState.CreateAccount)
    }

    fun changeToCamera() = viewModelScope.launch {
        loadingState.emit(LoginScreenState.Camera)
    }

    fun backToLanding() = viewModelScope.launch {
        loadingState.emit(LoginScreenState.LandingScreen)
    }

    fun changeToSignIn() = viewModelScope.launch {
        loadingState.emit(LoginScreenState.SignIn)
    }

    private fun createFirestoreUser(
        currentUser: FirebaseUser?,
        userName: String,
        profilePhoto: Photo?,
        navigateToHome: () -> Unit,
        context: Context,
    ) {

        viewModelScope.launch {
            val handle = Firebase.firestore.collection("users")
                .add(
                    hashMapOf(
                        "user_id" to currentUser!!.uid,
                        "user_name" to userName
                    )
                )
            handle.addOnSuccessListener {
                println("Successfully stored a user")
                Toast.makeText(context, "Signed in!", Toast.LENGTH_SHORT).show()
            }
            handle.addOnFailureListener {
                println("Failed to store user. ${it.localizedMessage}")
                println("Failed to store user. ${it.message}")
                Toast.makeText(context, "Failed to sign in.", Toast.LENGTH_SHORT).show()
            }
            uploadPhotos(profilePhoto!!, currentUser!!.uid)
            navigateToHome()
        }

    }

    fun signInWithEmailAndPassword(
        context: Context,
        email: String,
        password: String,
        navigateToHome: () -> Unit
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

    fun checkUserNameAvailability(userName: String, value: MutableState<Boolean>): Boolean {
        viewModelScope.launch(Dispatchers.IO) {
            val db = Firebase.firestore.collection("users")
            db.whereEqualTo("user_name", userName).get().addOnSuccessListener { result ->
                if (result.isEmpty) {
                    println("Nothing to report here. It was empty")
                    value.value = true
                    println("I changed the value - ${value.value}")
                } else {
                    value.value = false
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
        viewModelScope.launch(Dispatchers.IO) {
            Firebase.firestore.collection("users").whereEqualTo("user_id", id).get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        println("Error in updating the photo")
                        return@addOnSuccessListener
                    } else {
                        result.documents.forEach {
                            it.reference.update("profile_avatar", remoteUri)
                        }
                    }
                }
        }
    }

    fun setCurrentUser(currentUser: FirebaseUser?) = viewModelScope.launch {
        if (currentUser != null) {
            loadingState.emit(LoginScreenState.SignIn)
        }
    }
    fun googleSignIn(context: Context, token: String) {
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(token)
                .requestEmail().build()
        val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)

    }
    fun forgotPassword(email: String, context: Context) {
        Firebase.auth.sendPasswordResetEmail(email).addOnSuccessListener {
            println("Email was sent!")
        }.addOnFailureListener {
            println("A problem occurred: ${it.localizedMessage}")
            Toast.makeText(context, "An error has occurred: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }


}

