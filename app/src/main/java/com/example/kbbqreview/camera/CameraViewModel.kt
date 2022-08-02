package com.example.kbbqreview.camera

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel() {
    lateinit var photoUri: Uri

    val showPhotoRow = mutableStateOf(false)


    val EMPTY_IMAGE_URI: Uri = Uri.parse("file://dev/null")

    var imageUri = { mutableStateOf(EMPTY_IMAGE_URI) }

}