package com.example.kbbqreview

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kbbqreview.data.roomplaces.StoredPlaceViewModel
import com.example.kbbqreview.screens.addreview.ReviewViewModel
import com.example.kbbqreview.screens.map.MapViewModel
import com.example.kbbqreview.screens.map.location.LocationDetails
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    location: LocationDetails,
    focusManager: FocusManager,
    navController: NavHostController,
    reviewViewModel: ReviewViewModel
) {

    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: MapViewModel = MapViewModel()
    val storedPlaceViewModel: StoredPlaceViewModel = StoredPlaceViewModel(application)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(location.latitude, location.longitude), 17f)
    }
    val uiSettings = remember {
        MapUiSettings(zoomControlsEnabled = false)
    }

    val openDialog = remember { mutableStateOf(false) }
    val editMessage = remember { mutableStateOf("") }
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), 15f
                    )
                )
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_my_location_24),
                    contentDescription = "My Location"
                )
            }
        },
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    BottomNavigationItem(
                        icon = { Icon(screen.vector, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        GoogleMap(
            modifier = Modifier.padding(innerPadding),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            onMapLongClick = {
                viewModel.newMarkerPositionLat.value = it.latitude
                viewModel.newMarkerPositionLng.value = it.longitude
                viewModel.newMarkerState.value = true
                Toast.makeText(
                    context,
                    "Here is the current Lat Lng: ${it.latitude} and ${it.longitude}",
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            Marker(position = LatLng(location.latitude, location.longitude), flat = true)


            Marker(
                title = "Add review?",
                visible = viewModel.newMarkerState.value,
                position = LatLng(
                    viewModel.newMarkerPositionLat.value,
                    viewModel.newMarkerPositionLng.value
                ),
                onInfoWindowClick = { marker ->
                    reviewViewModel.newMarkerPositionLatReview.value = marker.position.latitude
                    reviewViewModel.newMarkerPositionLngReview.value = marker.position.longitude
                    navController.navigate(Screen.AddReview.route)
                    println("It changed the values.")
                }
            )
        }
    }
}



