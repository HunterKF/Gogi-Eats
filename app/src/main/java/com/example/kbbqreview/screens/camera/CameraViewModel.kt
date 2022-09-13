package com.example.kbbqreview.screens.camera

import android.app.Application
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.service.controls.Control
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.kbbqreview.data.photos.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

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