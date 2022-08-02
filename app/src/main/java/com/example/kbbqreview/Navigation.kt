package com.example.kbbqreview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.kbbqreview.camera.CameraPreview
import com.example.kbbqreview.camera.CameraViewModel
import com.example.kbbqreview.camera.MainContentCamera
import com.example.kbbqreview.screens.story.AddReview
import com.example.kbbqreview.screens.story.Story

@Composable
fun Navigation(
    navController: NavHostController,
    applicationViewModel: ApplicationViewModel
) {
    val focusManager = LocalFocusManager.current

    val location by applicationViewModel.getLocationLiveData().observeAsState()

    val startDestination = remember {
        mutableStateOf(Screen.Story.route)
    }

    val cameraViewModel = CameraViewModel()


    NavHost(
        navController,
        startDestination = startDestination.value,
    ) {
        composable(Screen.MapScreen.route) {
            location?.let { location ->
                MapScreen(
                    location = location,
                    focusManager = focusManager,
                    navController = navController
                )
            }
        }
        composable(Screen.Story.route) {
            Story(navController)
        }
        composable(Screen.AddReview.route) {
            AddReview(
                focusManager = focusManager,
                navController = navController,
                cameraViewModel = cameraViewModel
            )
            startDestination.value = Screen.AddReview.route
        }
        composable(Screen.MainContentCamera.route) {
            MainContentCamera(
                Modifier.fillMaxSize(),
                cameraViewModel = cameraViewModel,
                navController = navController
            )
        }

    }
}