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
import com.example.kbbqreview.screens.addreview.ReviewViewModel
import com.example.kbbqreview.screens.camera.CameraViewModel
import com.example.kbbqreview.screens.camera.MainContentCamera
import com.example.kbbqreview.screens.map.currentLocation.ChooseLocationMap
import com.example.kbbqreview.screens.story.AddReview
import com.example.kbbqreview.screens.story.Profile
import com.example.kbbqreview.screens.story.Story
import com.example.kbbqreview.screens.story.StoryViewModel

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
    val reviewViewModel = ReviewViewModel()


    NavHost(
        navController,
        startDestination = startDestination.value,
    ) {
        composable(Screen.MapScreen.route) {
            location?.let { location ->
                MapScreen(
                    location = location,
                    focusManager = focusManager,
                    navController = navController,
                    reviewViewModel = reviewViewModel
                )
            }
        }
        composable(Screen.Story.route) {
            Story(navController, applicationViewModel = applicationViewModel)
        }
        composable(Screen.AddReview.route) {
            AddReview(
                focusManager = focusManager,
                navController = navController,
                cameraViewModel = cameraViewModel,
                reviewViewModel = reviewViewModel,
                location = location,
                applicationViewModel = applicationViewModel
            )
            startDestination.value = Screen.AddReview.route
        }
        composable(Screen.Profile.route) {
            Profile(navController, applicationViewModel)
        }
        composable(Screen.MainContentCamera.route) {
            MainContentCamera(
                Modifier.fillMaxSize(),
                cameraViewModel = cameraViewModel,
                navController = navController
            )
        }
        composable(Screen.ChooseLocationMap.route) {
            location?.let { location ->
                ChooseLocationMap(
                    location = location,
                    reviewViewModel = reviewViewModel,
                    navController = navController
                )
            }
        }

    }
}