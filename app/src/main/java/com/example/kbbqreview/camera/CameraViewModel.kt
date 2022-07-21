package com.example.kbbqreview.camera

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData

class CameraViewModel {
    val permissionGrantedCamera: MutableLiveData<Boolean> = MutableLiveData(false)
    var shouldShowCamera = mutableStateOf(false)
}