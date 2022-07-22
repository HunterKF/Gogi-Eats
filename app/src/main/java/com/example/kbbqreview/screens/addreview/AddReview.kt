package com.example.kbbqreview.screens.story

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kbbqreview.CameraView
import com.example.kbbqreview.R
import com.example.kbbqreview.Screen
import com.example.kbbqreview.camera.CameraViewModel
import com.example.kbbqreview.data.roomplaces.StoredPlace
import com.example.kbbqreview.data.roomplaces.StoredPlaceViewModel
import com.example.kbbqreview.items
import com.example.kbbqreview.screens.map.MapViewModel
import com.example.kbbqreview.ui.theme.spacing
import kotlinx.coroutines.launch

@Composable
fun AddReview(focusManager: FocusManager, navController: NavHostController) {
    val cameraViewModel = CameraViewModel()
    val showCamera = cameraViewModel.shouldShowCamera
    val scope = rememberCoroutineScope()
    val focusRequester = FocusRequester()

    val context = LocalContext.current
    val application = context.applicationContext as Application
    val storedPlaceViewModel = StoredPlaceViewModel(application)
    val mapViewModel = MapViewModel()

    Scaffold(
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
        Column(modifier = Modifier.padding(innerPadding)) {
            val textFieldState = remember {
                mutableStateOf("")
            }
            val valueMeat = remember {
                mutableStateOf(0)
            }
            val valueBanchan = remember {
                mutableStateOf(0)
            }
            val valueAmenities = remember {
                mutableStateOf(0)
            }
            val valueAtmosphere = remember {
                mutableStateOf(0)
            }

            fun onTextFieldChange(query: String) {
                textFieldState.value = query
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                        Text(
                            modifier = Modifier.padding(MaterialTheme.spacing.small),
                            text = "Review",
                            style = MaterialTheme.typography.h4
                        )
                        TextField(
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .fillMaxWidth(),
                            value = textFieldState.value,
                            singleLine = true,
                            onValueChange = { newValue ->
                                onTextFieldChange(newValue)
                            })
                        radioGroups(
                            value = valueMeat, title = "Meat",
                            focusManager = focusManager
                        )
                        radioGroups(
                            value = valueBanchan,
                            title = "Banchan",
                            focusManager = focusManager
                        )
                        radioGroups(
                            value = valueAmenities,
                            title = "Amenities",
                            focusManager = focusManager
                        )
                        radioGroups(
                            value = valueAtmosphere,
                            title = "Atmosphere",
                            focusManager = focusManager
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CancelButton(
                                modifier = Modifier.weight(1f)
                            ) {
                                navController.navigate(Screen.MapScreen.route)
                            }
                            submitButton(
                                modifier = Modifier
                                    .weight(2f)
                                    .padding(MaterialTheme.spacing.small),
                                storedPlaceViewModel = storedPlaceViewModel,
                                valueMeat = valueMeat,
                                valueBanchan = valueBanchan,
                                valueAmenities = valueAmenities,
                                valueAtmosphere = valueAtmosphere,
                                textFieldState = textFieldState,
                                mapViewModel = mapViewModel,
                                context = context
                            )
                            CameraButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(MaterialTheme.spacing.small),
                                showCamera = showCamera,
                                navController = navController
                            )
                        }

                    }
                }
                if (showCamera.value) {
                    CameraView(onImageCaptured = { uri, fromGallery ->
                        Log.d("TAG", "Image Uri Captured from Camera View")
//Todo : use the uri as needed

                    }, onError = { imageCaptureException ->
                        scope.launch {
                            Toast.makeText(context, "An error has occurred.", Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }


        }
    }


}


@Composable
fun CancelButton(modifier: Modifier, onClick: () -> Unit) {
    IconButton(onClick = { onClick }) {
        Icon(
            painter = painterResource(id = R.drawable.ic_outline_cancel),
            contentDescription = "Cancel review"
        )
    }
}

@Composable
fun radioGroups(value: MutableState<Int>, title: String, focusManager: FocusManager) {
    Column(
        modifier = Modifier.padding(
            horizontal = MaterialTheme.spacing.large,
            vertical = MaterialTheme.spacing.extraSmall
        )
    ) {
        Text(text = title, style = MaterialTheme.typography.h6, modifier = Modifier.padding(4.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .border(2.dp, Color.Cyan, shape = RoundedCornerShape(25.dp))
                .padding(horizontal = MaterialTheme.spacing.small)
                .fillMaxWidth()
        ) {
            RadioButton(
                selected = value.value == 1,
                onClick = {
                    value.value = 1
                    focusManager.clearFocus()
                })
            Text("1", modifier = Modifier.padding(end = MaterialTheme.spacing.small))
            RadioButton(
                selected = value.value == 2,
                onClick = {
                    value.value = 2
                    focusManager.clearFocus()
                })
            Text("2", modifier = Modifier.padding(end = MaterialTheme.spacing.small))
            RadioButton(
                selected = value.value == 3,
                onClick = {
                    value.value = 3
                    focusManager.clearFocus()
                })
            Text("3", modifier = Modifier.padding(end = MaterialTheme.spacing.small))
        }
    }

}

@Composable
fun CameraButton(
    showCamera: MutableState<Boolean>,
    modifier: Modifier,
    navController: NavHostController
) {

    IconButton(onClick = { /*showCamera.value = true */ navController.navigate(Screen.CameraContainer.route)}) {
        Icon(
            painter = painterResource(id = R.drawable.ic_outline_camera),
            contentDescription = "Open camera"
        )
    }
}

@Composable
fun submitButton(
    modifier: Modifier,
    storedPlaceViewModel: StoredPlaceViewModel,
    valueMeat: MutableState<Int>,
    valueBanchan: MutableState<Int>,
    valueAmenities: MutableState<Int>,
    valueAtmosphere: MutableState<Int>,
    textFieldState: MutableState<String>,
    mapViewModel: MapViewModel,
    context: Context

) {
    Button(modifier = Modifier, onClick = {
        if (valueMeat.value != 0 && valueBanchan.value != 0 && valueAmenities.value != 0 && valueAtmosphere.value != 0 && textFieldState.value != "") {
            /*storedPlaceViewModel.addStoredPlace(
                StoredPlace(
                    "",
                    textFieldState.value,
                    mapViewModel.newMarkerPositionLat.value,
                    mapViewModel.newMarkerPositionLat.value,
                    valueMeat.value,
                    valueBanchan.value,
                    valueAmenities.value,
                    valueAtmosphere.value
                )
            )*/
            storedPlaceViewModel.save(
                storedPlace = StoredPlace(
                    0L,
                    "",
                    textFieldState.value,
                    mapViewModel.newMarkerPositionLat.value,
                    mapViewModel.newMarkerPositionLat.value,
                    valueMeat.value,
                    valueBanchan.value,
                    valueAmenities.value,
                    valueAtmosphere.value
                )
            )

        } else {
            Toast.makeText(context, "Submit all the fields!", Toast.LENGTH_LONG).show()
        }

    }) {
        Text("Submit Review")
    }
}
