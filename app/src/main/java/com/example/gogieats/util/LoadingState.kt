package com.example.gogieats.util

sealed class LoginScreenState {
    object Loading : LoginScreenState()
    object LandingScreen : LoginScreenState()
    object SignIn : LoginScreenState()
    object CreateAccount : LoginScreenState()
    object CreateAccCamera : LoginScreenState()
    object ChangeSettingCamera : LoginScreenState()
    object ChangeProfileSettings : LoginScreenState()
    data class Error(
        val error: String
    ) : LoginScreenState()
}