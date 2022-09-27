package com.example.kbbqreview.util

sealed class LoginScreenState {
    object Loading : LoginScreenState()
    object LandingScreen : LoginScreenState()
    object SignIn : LoginScreenState()
    object CreateAccount : LoginScreenState()
    object Camera : LoginScreenState()
    data class Error(
        val error: String
    ) : LoginScreenState()
}