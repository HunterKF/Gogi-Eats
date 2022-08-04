package com.example.kbbqreview.screens.story

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.example.kbbqreview.*
import com.example.kbbqreview.R
import com.example.kbbqreview.screens.camera.CameraViewModel
import com.example.kbbqreview.data.roomplaces.StoredPlace
import com.example.kbbqreview.data.roomplaces.StoredPlaceViewModel
import com.example.kbbqreview.screens.addreview.ReviewViewModel
import com.example.kbbqreview.screens.map.MapViewModel
import com.example.kbbqreview.ui.theme.spacing

@Composable
fun AddReview(
    focusManager: FocusManager,
    navController: NavHostController,
    cameraViewModel: CameraViewModel,
    reviewViewModel: ReviewViewModel
) {
    val focusRequester = FocusRequester()

    val context = LocalContext.current
    val application = context.applicationContext as Application
    val storedPlaceViewModel = StoredPlaceViewModel(application)
    val mapViewModel = MapViewModel()
    val TAG = "CAMERA TAG"
    val allPhotos = cameraViewModel.getAllPhotos()

    Log.d(TAG, "Current value from AddReview of showRow is: ${cameraViewModel.showPhotoRow.value}")

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

            fun onTextFieldChange(query: String) {
                reviewViewModel.textFieldState.value = query
            }

            val intent = (context as MainActivity).intent
            val photoUri = intent.getStringExtra("image")

            val lazyState = rememberLazyListState()
            val columnState = rememberScrollState()
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {


                LazyColumn(
                    modifier = Modifier
                        .padding(MaterialTheme.spacing.medium)
                        .scrollable(state = columnState, orientation = Orientation.Horizontal)
                ) {
                    item {
                        Text(
                            modifier = Modifier.padding(MaterialTheme.spacing.small),
                            text = "Review",
                            style = MaterialTheme.typography.h4
                        )
                    }

                    item {
                        TextField(
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .fillMaxWidth(),
                            value = reviewViewModel.textFieldState.value,
                            singleLine = true,
                            onValueChange = { newValue ->
                                onTextFieldChange(newValue)
                            }
                        )
                    }
                    item {
                        radioGroups(
                            value = reviewViewModel.valueMeat, title = "Meat",
                            focusManager = focusManager
                        )
                        radioGroups(
                            value = reviewViewModel.valueBanchan,
                            title = "Banchan",
                            focusManager = focusManager
                        )
                        radioGroups(
                            value = reviewViewModel.valueAmenities,
                            title = "Amenities",
                            focusManager = focusManager
                        )
                        radioGroups(
                            value = reviewViewModel.valueAtmosphere,
                            title = "Atmosphere",
                            focusManager = focusManager
                        )
                    }
                    if (cameraViewModel.showPhotoRow.value) {
                        item {
                            LazyRow(modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)) {
                                items(allPhotos) { uri ->
                                    ImageCard(
                                        uri = uri,
                                        cameraViewModel = cameraViewModel,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }

                            }
                        }

                    }
                    item {
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
                                valueMeat = reviewViewModel.valueMeat,
                                valueBanchan = reviewViewModel.valueBanchan,
                                valueAmenities = reviewViewModel.valueAmenities,
                                valueAtmosphere = reviewViewModel.valueAtmosphere,
                                textFieldState = reviewViewModel.textFieldState,
                                mapViewModel = mapViewModel,
                                context = context
                            )
                            CameraButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(MaterialTheme.spacing.small),
                                navController = navController,
                                context = context
                            )
                        }
                    }


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
            var sliderPosition by remember { mutableStateOf(0f) }
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it
                                value.value = it.toInt()
                                println(value.value)
                                },
                valueRange = 1f..3f,
                onValueChangeFinished = {
                    // launch some business logic update with the state you hold
                    // viewModel.updateSelectedSliderValue(sliderPosition)
                },
                steps = 1,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colors.secondary,
                    activeTrackColor = MaterialTheme.colors.secondary
                )
            )

            Text("3", modifier = Modifier.padding(end = MaterialTheme.spacing.small))
        }
    }
    @Composable
    fun VerticalLines(dates: List<String>) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        ) {
            val drawPadding: Float = with(LocalDensity.current) { 10.dp.toPx() }
            Canvas(modifier = Modifier.fillMaxSize()) {
                val yStart = 0f
                val yEnd = size.height
                val distance: Float = (size.width.minus(2 * drawPadding)).div(dates.size.minus(1))
                dates.forEachIndexed { index, step ->
                    drawLine(
                        color = Color.Red,
                        start = Offset(x = drawPadding + index.times(distance), y = yStart),
                        end = Offset(x = drawPadding + index.times(distance), y = yEnd)
                    )
                }
            }
        }
    }

}

@Composable
fun CameraButton(
    navController: NavHostController,
    context: Context,
    modifier: Modifier
) {
    val context = LocalContext.current
    IconButton(onClick = { /*showCamera.value = true */ navController.navigate(Screen.MainContentCamera.route) }) {
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

@Composable
fun ImageCard(uri: Uri, cameraViewModel: CameraViewModel, modifier: Modifier) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(15.dp),
        elevation = 5.dp
    ) {
        Box(
            modifier = Modifier
                .height(150.dp)
                .width(100.dp)
        ) {
            Image(
                contentScale = ContentScale.Crop,
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Captured image",
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                onClick = {
                    cameraViewModel.removeOnePhoto(uri)
                    if (cameraViewModel.selectImages.isEmpty()) {
                        cameraViewModel.showPhotoRow.value = false
                    }

                }
            ) {
                Icon(
                    modifier = Modifier.rotate(45f),
                    painter = painterResource(id = R.drawable.ic_outline_add),
                    contentDescription = "Image"
                )
            }
        }
    }
}


