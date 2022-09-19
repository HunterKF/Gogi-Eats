package com.example.kbbqreview.screens.camera

import android.util.Log
import android.widget.Gallery
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.kbbqreview.Screen
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.camera.gallery.GallerySelectMultiple
import com.example.kbbqreview.screens.camera.gallery.GallerySelectSingle
import com.example.kbbqreview.screens.login.LoginViewModel

@Composable
fun ProfileCamera(
    modifier: Modifier = Modifier,
    cameraViewModel: CameraViewModel,
    viewModel: LoginViewModel
) {
    val TAG = "CAMERA TAG"
    var imageUri by remember { mutableStateOf(cameraViewModel.EMPTY_IMAGE_URI) }
    val imageUri2 = remember { mutableListOf(cameraViewModel.EMPTY_IMAGE_URI) }

    Log.d(TAG, "The camera has opened.")
    if (imageUri != cameraViewModel.EMPTY_IMAGE_URI) {
        Box(modifier = modifier) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = rememberImagePainter(imageUri),
                contentDescription = "Captured image"
            )
            //this deletes the URI and sets it back to empty URI
            Button(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClick = {
                    cameraViewModel.selectImages.add(Photo(localUri = imageUri.toString()))
                    cameraViewModel.showPhotoRow.value = true
                    viewModel.changeToCreate()
                    imageUri = cameraViewModel.EMPTY_IMAGE_URI
                }
            ) {
                Text("Select image")
            }
        }
    } else {
        var showGallerySelect by remember { mutableStateOf(false) }
        if (showGallerySelect) {
            GallerySelectSingle(
                modifier = modifier,
                cameraViewModel = cameraViewModel,
                onImageUri = { uri ->

                    cameraViewModel.selectImages.add(Photo(localUri = uri.toString()))
                    showGallerySelect = false

                    cameraViewModel.showPhotoRow.value = true
                    viewModel.changeToCreate()
                }
            )
        } else {
            Box(modifier = modifier) {
                //This is the camera function
                CameraCapture(
                    modifier = modifier,
                    onImageFile = { file ->
                        imageUri = file.toUri()

                    }
                )
                Button(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(4.dp),
                    onClick = {
                        showGallerySelect = true
                    }
                ) {
                    Text("Select from Gallery")
                }
            }
        }
    }
    BackHandler() {
        viewModel.changeToCreate()
    }
}