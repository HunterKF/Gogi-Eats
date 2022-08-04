package com.example.kbbqreview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.kbbqreview.screens.camera.ui.theme.KBBQReviewTheme

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

