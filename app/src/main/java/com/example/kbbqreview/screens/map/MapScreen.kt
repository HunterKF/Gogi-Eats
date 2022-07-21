package com.example.kbbqreview

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.kbbqreview.data.location.LocationDetails
import com.example.kbbqreview.data.roomplaces.StoredPlaceViewModel
import com.example.kbbqreview.screens.map.MapViewModel
import com.example.kbbqreview.screens.map.ReviewDialog
import com.example.kbbqreview.ui.theme.spacing
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    location: LocationDetails,
    focusManager: FocusManager
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
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.latitude,
                        location.longitude
                    ), 15f
                )
            ) }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_my_location_24),
                    contentDescription = "My Location"
                )
            }
        }
    ) {

        GoogleMap(
            modifier = Modifier,
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
                    openDialog.value = true
                    Toast.makeText(
                        context,
                        "Here is the current Lat Lng: ${marker.id} and $marker",
                        Toast.LENGTH_LONG
                    ).show()

                }
            )


        }

    }

    if (openDialog.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = contentColorFor(MaterialTheme.colors.background)
                        .copy(alpha = 0.6f)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        openDialog.value = false
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(MaterialTheme.spacing.medium)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            openDialog.value = true
                        })
            ) {
                ReviewDialog(
                    openDialog = openDialog, context = context,
                    storedPlaceViewModel = storedPlaceViewModel,
                    mapViewModel = viewModel,
                    focusManager = focusManager
                )
            }

        }
    }
}



