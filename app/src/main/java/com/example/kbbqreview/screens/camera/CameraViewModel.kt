package com.example.kbbqreview.screens.camera

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kbbqreview.data.photos.Photo

class CameraViewModel : ViewModel() {
    lateinit var photoUri: Uri

    val showPhotoRow = mutableStateOf(false)
    val checkPhoto = mutableStateOf(false)

    val EMPTY_IMAGE_URI: Uri = Uri.parse("file://dev/null")


    var selectImages =  mutableStateListOf<Photo>()

    fun getAllPhotos(): SnapshotStateList<Photo> {
        return  selectImages
    }
    fun removeOnePhoto(photo: Photo) {
        selectImages.remove(photo)
    }
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