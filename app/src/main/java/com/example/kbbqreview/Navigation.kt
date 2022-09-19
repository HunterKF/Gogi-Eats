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
import com.example.kbbqreview.screens.camera.MainCamera
import com.example.kbbqreview.screens.map.currentLocation.FromAddChooseLocation
import com.example.kbbqreview.screens.HomeScreen.AddReview
import com.example.kbbqreview.screens.HomeScreen.ProfileScreen
import com.example.kbbqreview.screens.HomeScreen.HomeScreen
import com.example.kbbqreview.screens.camera.ProfileCamera
import com.example.kbbqreview.screens.login.LoginScreen
import com.example.kbbqreview.screens.login.LoginViewModel
import com.example.kbbqreview.screens.map.currentLocation.FromEditChooseLocation
import com.example.kbbqreview.screens.profile.ProfileViewModel

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
    val profileViewModel = ProfileViewModel()
    val LoginViewModel = LoginViewModel()


    NavHost(
        navController,
        startDestination = startDestination.value,
    ) {
        composable(Screen.MapScreen.route) {
            location?.let { location ->
                MapScreen(
                    location = location,
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
                applicationViewModel = applicationViewModel,
                addReviewViewModel = reviewViewModel
            )
            startDestination.value = Screen.AddReview.route
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                location = location,
                profileViewModel = profileViewModel,
                cameraViewModel = cameraViewModel
                ) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Profile.route) {
                        inclusive = true
                    }
                }

            }
        }
        composable(Screen.Login.route) {
            LoginScreen(
                navigateToHome = {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }, popBackStack = {
                    navController.popBackStack()
                }, navigateToCamera = {
                    navController.navigate(Screen.MainCamera.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }, navigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                },
                viewModel = LoginViewModel,
                navController = navController,
                cameraViewModel = cameraViewModel
            )
        }
        composable(Screen.MainCamera.route) {
            MainCamera(
                Modifier.fillMaxSize(),
                cameraViewModel = cameraViewModel,
                navController = navController
            )
        }
        composable(Screen.ProfileCamera.route) {
            ProfileCamera(
                Modifier.fillMaxSize(),
                cameraViewModel = cameraViewModel,
                viewModel = LoginViewModel
            )
        }
        composable(Screen.FromAddChooseLocation.route) {
            location?.let { location ->
                FromAddChooseLocation(
                    location = location,
                    reviewViewModel = reviewViewModel,
                    navController = navController
                )
            }
        }
        composable(Screen.FromEditChooseLocation.route) {
            location?.let { location ->
                FromEditChooseLocation(
                    location = location,
                    profileViewModel = profileViewModel,
                    navController = navController
                )
            }
        }
    }
}