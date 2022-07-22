package com.example.kbbqreview

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kbbqreview.camera.CameraContainer
import com.example.kbbqreview.screens.story.AddReview
import com.example.kbbqreview.screens.story.Story

@Composable
fun Navigation(
    navController: NavHostController,
    applicationViewModel: ApplicationViewModel
) {
    val focusManager = LocalFocusManager.current

    val location by applicationViewModel.getLocationLiveData().observeAsState()


        NavHost(
            navController,
            startDestination = Screen.MapScreen.route,
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
                AddReview(focusManager = focusManager, navController = navController)
            }
            composable(Screen.CameraContainer.route) {
                CameraContainer()
            }

        }
}