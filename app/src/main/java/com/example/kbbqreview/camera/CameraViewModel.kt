package com.example.kbbqreview.camera

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel() {
    lateinit var photoUri: Uri

    val showPhotoRow = mutableStateOf(false)
    val checkPhoto = mutableStateOf(false)

    val EMPTY_IMAGE_URI: Uri = Uri.parse("file://dev/null")


    var imageUri = { mutableStateOf(EMPTY_IMAGE_URI) }

    var selectImages =  mutableListOf<Uri>()
    var selectImagesLiveData =  MutableLiveData<List<Uri>>()
    private val list = mutableListOf<Uri>()
    private val _photoList = MutableLiveData<List<Uri>>()
    val photoList: LiveData<List<Uri>> = _photoList

    init {
        _photoList.value = list
    }
    fun addPhoto(uri: Uri) {
        list.add(uri)
        _photoList.value = list
    }
    fun deleteOne(uri: Uri) {
       list.remove(uri)
        _photoList.value = list
    }

}