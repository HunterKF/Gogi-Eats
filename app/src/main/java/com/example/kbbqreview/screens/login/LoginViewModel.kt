package com.example.kbbqreview.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kbbqreview.util.LoginScreenState
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {


    val loadingState = MutableStateFlow<LoginScreenState>(LoginScreenState.LandingScreen)

    val state = loadingState.asStateFlow()

    fun createAccount(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            loadingState.emit(LoginScreenState.Loading)
            Firebase.auth.createUserWithEmailAndPassword(email, password).await()
            val currentUser = Firebase.auth.currentUser
            createFirestoreUser(currentUser)
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

    private fun createFirestoreUser(currentUser: FirebaseUser?) {
        /* val handle = Firebase.firestore.collection("users")
             .add(
                 hashMapOf(
                     "user_id" to currentUser!!.uid
                 )
             )*/
        viewModelScope.launch {
            loadingState.emit(LoginScreenState.CreateAccount)
        }

    }

    fun signInWithEmailAndPassword(email: String, password: String) = viewModelScope.launch {
        try {
            println("Trying to login...")
            loadingState.emit(LoginScreenState.Loading)
            Firebase.auth.signInWithEmailAndPassword(email, password).await()
            loadingState.emit(LoginScreenState.CreateAccount)
        } catch (e: Exception) {
            println("Something failed... ${e.localizedMessage}")
            loadingState.emit(LoginScreenState.Error(e.localizedMessage))
        }
    }
}
