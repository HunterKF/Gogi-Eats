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
import com.example.kbbqreview.screens.HomeScreen.AddReview
import com.example.kbbqreview.screens.HomeScreen.Profile
import com.example.kbbqreview.screens.HomeScreen.HomeScreen

@Composable
fun Navigation(
    navController: NavHostController,
    applicationViewModel: ApplicationViewModel
) {
    val focusManager = LocalFocusManager.current
    val location by applicationViewModel.getLocationLiveData().observeAsState()

    val startDestination = remember {
        mutableStateOf(Screen.HomeScreen.route)
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
        composable(Screen.HomeScreen.route) {
            HomeScreen(
                navController = navController,
                applicationViewModel = applicationViewModel
            ) {
                navController.navigate("signin") {
                    popUpTo("home") {
                        inclusive = true
                    }
                }
            }
        }
        composable(Screen.AddReview.route) {
            AddReview(
                focusManager = focusManager,
                navController = navController,
                cameraViewModel = cameraViewModel,
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