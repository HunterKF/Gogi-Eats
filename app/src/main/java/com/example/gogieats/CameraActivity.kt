package com.example.gogieats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.gogieats.screens.camera.ui.theme.KBBQReviewTheme

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

