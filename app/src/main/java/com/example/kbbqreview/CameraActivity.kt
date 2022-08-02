package com.example.kbbqreview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberImagePainter
import com.example.kbbqreview.camera.CameraCapture
import com.example.kbbqreview.camera.CameraViewModel
import com.example.kbbqreview.camera.gallery.GallerySelect
import com.example.kbbqreview.camera.ui.theme.KBBQReviewTheme

class CameraActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KBBQReviewTheme {

//                MainContent(Modifier.fillMaxSize(), cameraViewModel = cameraViewModel)

            }
        }
    }
}

