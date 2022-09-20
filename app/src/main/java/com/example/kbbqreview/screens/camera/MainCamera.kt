package com.example.kbbqreview.screens.camera

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.kbbqreview.R
import com.example.kbbqreview.Screen
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.camera.gallery.GallerySelectMultiple

@Composable
fun MainCamera(
    modifier: Modifier = Modifier,
    cameraViewModel: CameraViewModel,
    navController: NavController,
) {
    val TAG = "CAMERA TAG"
    var imageUri by remember { mutableStateOf(cameraViewModel.EMPTY_IMAGE_URI) }
    val imageUri2 = remember { mutableListOf(cameraViewModel.EMPTY_IMAGE_URI) }

    Log.d(TAG, "The camera has opened.")
    if (imageUri != cameraViewModel.EMPTY_IMAGE_URI) {
        Box(modifier = modifier.background(Color.Black)) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = rememberImagePainter(imageUri),
                contentDescription = stringResource(R.string.captured_image)
            )
            //this deletes the URI and sets it back to empty URI
            Box(Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 28.dp, vertical = 40.dp)
                .fillMaxWidth())
            {
                IconButton(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .scale(1.2f),
                    onClick = {
                        cameraViewModel.selectImages.add(Photo(localUri = imageUri.toString()))
                        cameraViewModel.showPhotoRow.value = true
                        navController.popBackStack()
                        imageUri = cameraViewModel.EMPTY_IMAGE_URI
                    }
                ) {
                    Icon(Icons.Rounded.Check, stringResource(R.string.take_picture), tint = Color.White)
                }
                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .scale(1.2f),
                    onClick = {/*
                        navController.popBackStack()*/
                        imageUri = cameraViewModel.EMPTY_IMAGE_URI
                    }
                ) {
                    Icon(Icons.Rounded.Delete, stringResource(R.string.take_picture), tint = Color.White)
                }
            }

        }
    } else {
        var showGallerySelect = remember { mutableStateOf(false) }
        if (showGallerySelect.value) {
            GallerySelectMultiple(
                modifier = modifier,
                cameraViewModel = cameraViewModel,
                onImageUri = { uri ->
                    cameraViewModel.selectImages.add(Photo(localUri = uri.toString()))
                    showGallerySelect.value = false

                    cameraViewModel.showPhotoRow.value = true
                    navController.popBackStack(Screen.MainCamera.route, inclusive = true)
                }
            )
        } else {
            Box(modifier = modifier) {
                //This is the camera function
                CameraCapture(
                    showGallerySelect = showGallerySelect,
                    modifier = modifier,
                    onImageFile = { file ->
                        imageUri = file.toUri()

                    })

            }

        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showSystemUi = true)
@Composable
fun Preview() {
    Box() {
        //This is the camera function
        IconButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 60.dp),
            onClick = {
            }
        ) {
            Icon(Icons.Rounded.PhotoLibrary, null, tint = Color.White, modifier = Modifier
                .scale(3f))
        }
        IconButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 60.dp),
            onClick = {
            }
        ) {
            Icon(Icons.Rounded.PhotoLibrary, null, tint = Color.White, modifier = Modifier
                .scale(3f))
        }
    }
}