package com.example.kbbqreview.screens.HomeScreen

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.example.kbbqreview.*
import com.example.kbbqreview.R
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.camera.CameraViewModel
import com.example.kbbqreview.data.roomplaces.StoredPlaceViewModel
import com.example.kbbqreview.screens.addreview.ReviewViewModel
import com.example.kbbqreview.screens.map.MapViewModel
import com.example.kbbqreview.screens.map.location.LocationDetails
import com.example.kbbqreview.ui.theme.spacing

@Composable
fun AddReview(
    focusManager: FocusManager,
    navController: NavHostController,
    cameraViewModel: CameraViewModel,
    location: LocationDetails?,
    applicationViewModel: ApplicationViewModel
) {
    val focusRequester = FocusRequester()

    val context = LocalContext.current
    val application = context.applicationContext as Application
    val TAG = "CAMERA TAG"
    val allPhotos = cameraViewModel.getAllPhotos()

    val addReviewViewModel = ReviewViewModel()


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
        val innerPadding = innerPadding


        val intent = (context as MainActivity).intent
        val photoUri = intent.getStringExtra("image")

        val lazyState = rememberLazyListState()
        Surface {
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .scrollable(state = lazyState, orientation = Orientation.Horizontal),

                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    Text(
                        modifier = Modifier.padding(MaterialTheme.spacing.small),
                        text = "Review",
                        style = MaterialTheme.typography.h4
                    )
                }

                item {

                    //restaurant
                    OutlinedTextField(
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .fillMaxWidth(),
                        value = addReviewViewModel.restaurantNameText.value,
                        singleLine = true,
                        onValueChange = { newValue ->
                            addReviewViewModel.onTextFieldChange(
                                addReviewViewModel.restaurantNameText,
                                newValue
                            )
                        }
                    )
                }
                item {
                    //address
                    LocationBar(
                        applicationViewModel = applicationViewModel,
                        reviewViewModel = addReviewViewModel,
                        location = location,
                        navController = navController,
                        focusManager = focusManager
                    )
                }
                item {
                    //review values
                    reviewBar(
                        value = addReviewViewModel.valueMeat, title = "Meat",
                        focusManager = focusManager
                    )
                    reviewBar(
                        value = addReviewViewModel.sideDishes,
                        title = "Banchan",
                        focusManager = focusManager
                    )
                    reviewBar(
                        value = addReviewViewModel.valueAmenities,
                        title = "Amenities",
                        focusManager = focusManager
                    )
                    reviewBar(
                        value = addReviewViewModel.valueAtmosphere,
                        title = "Atmosphere",
                        focusManager = focusManager
                    )
                }
                item {
                    //written review
                    ReviewTextfield(
                        modifier = Modifier.fillMaxWidth(),
                        reviewViewModel = addReviewViewModel
                    )
                }
                if (cameraViewModel.showPhotoRow.value) {
                    //photos
                    item {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            items(allPhotos) { photo ->
                                ImageCard(
                                    photo = photo,
                                    cameraViewModel = cameraViewModel,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                        }
                    }

                }
                item {
                    //Submit bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CancelButton(
                            modifier = Modifier.weight(1f)
                        ) {

                        }
                        Button(onClick = {
                            println(applicationViewModel.firebaseUser)
                            Log.d(
                                "Firebase Auth",
                                "The value is now: ${applicationViewModel.firebaseUser}"
                            )
                            Toast.makeText(
                                context,
                                "The value for user is: ${applicationViewModel.user}",
                                Toast.LENGTH_LONG
                            ).show()
                        }) {
                            Text("test")
                        }
                        SubmitButton(
                            modifier = Modifier
                                .weight(2f)
                                .padding(MaterialTheme.spacing.small),
                            cameraViewModel = cameraViewModel,
                            reviewViewModel = addReviewViewModel,
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


@Composable
fun ReviewTextfield(reviewViewModel: ReviewViewModel, modifier: Modifier) {
    val context = LocalContext.current
    val currentCharCount = remember { mutableStateOf(0) }
    val maxChars = 1000

    Text(text = "(Optional) Write about it", style = MaterialTheme.typography.subtitle1)
    Box() {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            value = reviewViewModel.restaurantReviewText.value,
            onValueChange = { newValue ->
                if (newValue.length <= maxChars) {
                    currentCharCount.value = newValue.length
                    reviewViewModel.onTextFieldChange(
                        reviewViewModel.restaurantReviewText,
                        newValue
                    )
                } else {
                    Toast.makeText(context, "Shorten review.", Toast.LENGTH_SHORT).show()
                }

            }

        )
        Text(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-2).dp, y = (-4).dp),
            text = "${currentCharCount.value} / 1000"
        )
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
fun reviewBar(value: MutableState<Int>, title: String, focusManager: FocusManager) {
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

            var sliderPosition by remember { mutableStateOf(value.value.toFloat()) }
            Slider(
                value = sliderPosition,
                onValueChange = {
                    focusManager.clearFocus()
                    sliderPosition = it
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
fun SubmitButton(
    modifier: Modifier,
    cameraViewModel: CameraViewModel,
    reviewViewModel: ReviewViewModel,
    context: Context,

    ) {
    Button(modifier = Modifier, onClick = {

        when {
            reviewViewModel.restaurantLng.value == 0.0 && reviewViewModel.restaurantLat.value == 0.0 -> {
                Toast.makeText(context, "Add location!", Toast.LENGTH_SHORT).show()
            }
            cameraViewModel.selectImages.isEmpty() -> {
                Toast.makeText(context, "Add a photo!", Toast.LENGTH_SHORT).show()
            }
            reviewViewModel.restaurantNameText.value == "" -> {
                Toast.makeText(context, "Add a name!", Toast.LENGTH_SHORT).show()
            }
            else -> {
//                reviewViewModel.uploadPhotos(selectImages = cameraViewModel.selectImages)
                reviewViewModel.onSubmitButton(selectImages = cameraViewModel.selectImages)

                Toast.makeText(context, "Review saved!", Toast.LENGTH_SHORT).show()
                /*
                applicationViewModel.uploadPhotos(
                    cameraViewModel.selectImages,
                    storedPlace = StoredPlace()
                )*/

                /*GlobalScope.launch {
                val compressedImage = storedPlaceViewModel.compressImage(
                    context as ComponentActivity,
                    allPhotos[0]
                )
                storedPlaceViewModel.uploadPhoto(
                    compressedImage!!,
                    "image.jpg",
                    "image/jpg"
                ) {
                    GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Filed uploaded!", Toast.LENGTH_SHORT).show()
                    }
                }
            }*/
            }
        }
    }
    ) {
        Text("Submit")
    }
}

@Composable
fun ImageCard(photo: Photo, cameraViewModel: CameraViewModel, modifier: Modifier) {
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
                painter = rememberAsyncImagePainter(photo.localUri),
                contentDescription = "Captured image",
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                onClick = {
                    cameraViewModel.removeOnePhoto(photo)
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


@Composable
fun LocationBar(
    applicationViewModel: ApplicationViewModel,
    reviewViewModel: ReviewViewModel,
    location: LocationDetails?,
    navController: NavHostController,
    focusManager: FocusManager
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(5.dp)
            )
            .border(1.dp, Color.Cyan),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(3f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = reviewViewModel.address.value)
        }
        IconButton(
            modifier = Modifier.weight(1f),
            onClick = {
                focusManager.clearFocus()
                applicationViewModel.startLocationUpdates()
                reviewViewModel.changeLocation(
                    location!!.latitude,
                    location!!.longitude,
                    context = context
                )

            }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_my_location_24),
                contentDescription = "My location"
            )
        }
        IconButton(
            modifier = Modifier.weight(1f),
            onClick = {
                focusManager.clearFocus()
                navController.navigate(Screen.ChooseLocationMap.route)
            }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_outline_map),
                contentDescription = "Open map"
            )
        }
    }

}



