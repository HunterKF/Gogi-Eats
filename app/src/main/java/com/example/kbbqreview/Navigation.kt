package com.example.kbbqreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun Navigation(
    navController: NavHostController,
    applicationViewModel: ApplicationViewModel
) {
    val focusManager = LocalFocusManager.current

    val location by applicationViewModel.getLocationLiveData().observeAsState()

    NavHost(navController = navController, startDestination = Screen.MapScreen.route) {
        composable(route = Screen.MapScreen.route) {

            location?.let { location ->
                MapScreen(
                    location = location,
                    focusManager = focusManager
                )
            }
        }
    }
}