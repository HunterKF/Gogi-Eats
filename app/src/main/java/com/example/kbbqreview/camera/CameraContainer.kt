package com.example.kbbqreview.camera

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.kbbqreview.CameraView
import kotlinx.coroutines.launch

@Composable
fun CameraContainer() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    CameraView(onImageCaptured = { uri, fromGallery ->
        Log.d("TAG", "Image Uri Captured from Camera View")
        //Todo : use the uri as needed

    }, onError = { imageCaptureException ->
        scope.launch {
            Toast.makeText(context, "An error has occurred.", Toast.LENGTH_LONG).show()
        }
    })
}
