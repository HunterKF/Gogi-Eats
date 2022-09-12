package com.example.kbbqreview.screens.map.currentLocation

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kbbqreview.R
import com.example.kbbqreview.Screen
import com.example.kbbqreview.screens.addreview.ReviewViewModel
import com.example.kbbqreview.screens.map.MapStyle
import com.example.kbbqreview.screens.map.location.LocationDetails
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun ChooseLocationMap(
    location: LocationDetails,
    reviewViewModel: ReviewViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val uiSettings = remember {
        MapUiSettings(zoomControlsEnabled = false)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(location.latitude, location.longitude), 17f)
    }
    val confirmState = remember {
        mutableStateOf(false)
    }
    GoogleMap(
        modifier = androidx.compose.ui.Modifier.padding(),
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = MapProperties(
            mapStyleOptions = MapStyleOptions(MapStyle.json)
        )
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .border(1.dp, Color.Blue)
        ) {
            if (confirmState.value) {
                Card(
                    Modifier
                        .align(Alignment.Center)
                        .offset(y = (-75).dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Select here?",
                            textAlign = TextAlign.Center,
                            fontSize = MaterialTheme.typography.h5.fontSize
                        )
                        Row(
                            Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = {
                                reviewViewModel.changeLocation(
                                    cameraPositionState.position.target.latitude,
                                    cameraPositionState.position.target.longitude,
                                    context = context
                                )
                                navController.navigate(Screen.AddReview.route)
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_check_24),
                                    contentDescription = "Accept"
                                )
                            }
                            IconButton(onClick = {
                                confirmState.value = false
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_outline_cancel),
                                    contentDescription = "Cancel"
                                )
                            }
                        }
                    }

                }
            }
            IconButton(
                modifier = Modifier.align(Alignment.Center),
                onClick = {
                    confirmState.value = true
                },
            ) {
                Image(
                    modifier = Modifier.scale(2f),
                    painter = painterResource(id = R.drawable.ic_baseline_map_marker_24),
                    contentDescription = "Map marker"
                )
                /* Text(
                     text = "Is camera moving: ${cameraPositionState.isMoving}" +
                             "\n Latitude and Longitude: ${cameraPositionState.position.target.latitude} " +
                             "and ${cameraPositionState.position.target.longitude}",
                     textAlign = TextAlign.Center
                 )*/
            }
        }
    }
}