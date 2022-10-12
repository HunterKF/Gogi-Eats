package com.example.kbbqreview.screens.HomeScreen

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kbbqreview.*
import com.example.kbbqreview.R
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.AddNewPhoto
import com.example.kbbqreview.screens.camera.CameraViewModel
import com.example.kbbqreview.screens.addreview.ReviewViewModel
import com.example.kbbqreview.screens.map.location.LocationDetails
import com.example.kbbqreview.ui.theme.spacing
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode


@Composable
fun AddReview(
    focusManager: FocusManager,
    navController: NavHostController,
    cameraViewModel: CameraViewModel,
    location: LocationDetails?,
    applicationViewModel: ApplicationViewModel,
    addReviewViewModel: ReviewViewModel
) {
    val focusRequester = FocusRequester()

    val context = LocalContext.current
    val application = context.applicationContext as Application
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
        val innerPadding = innerPadding


        val intent = (context as MainActivity).intent
        val photoUri = intent.getStringExtra("image")

        val lazyState = rememberLazyListState()
        val currentCharCount = remember { mutableStateOf(0) }
        Surface {
            LazyColumn(
                modifier = Modifier
                    .scrollable(state = lazyState, orientation = Orientation.Horizontal),
                contentPadding = PaddingValues(
                    top = 10.dp,
                    bottom = 120.dp,
                    start = 12.dp,
                    end = 12.dp
                )
            ) {
                item {
                    Text(
                        modifier = Modifier.padding(MaterialTheme.spacing.small),
                        text = "Add Review",
                        style = MaterialTheme.typography.h4
                    )
                }

                item {
                    //restaurant
                    InputRestaurantName(focusRequester, addReviewViewModel)
                    Spacer(Modifier.height(8.dp))
                }
                item {
                    //address
                    Spacer(Modifier.height(8.dp))
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
                    ReviewBar(
                        value = addReviewViewModel.valueMeat, title = "Meat",
                        focusManager = focusManager,
                        R.drawable.meat_icon
                    )
                    ReviewBar(
                        value = addReviewViewModel.sideDishes,
                        title = "Banchan",
                        focusManager = focusManager,
                        R.drawable.side_dishes_icon
                    )
                    ReviewBar(
                        value = addReviewViewModel.valueAmenities,
                        title = "Amenities",
                        focusManager = focusManager,
                        R.drawable.amenities_icon
                    )
                    ReviewBar(
                        value = addReviewViewModel.valueAtmosphere,
                        title = "Atmosphere",
                        focusManager = focusManager,
                        R.drawable.atmosphere_icon
                    )
                }
                item {
                    CameraPhotos(
                        modifier = Modifier.fillMaxWidth(),
                        navController = navController,
                        allPhotos = allPhotos
                    )
                }
                item {
                    //written review

                    ReviewTextfield(
                        modifier = Modifier.fillMaxWidth(),
                        reviewViewModel = addReviewViewModel,
                        currentCharCount = currentCharCount
                    )
                }
                item {
                    //Submit bar
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            println(applicationViewModel.firebaseUser)
                            Log.d(
                                "Firebase Auth",
                                "The value is now: ${applicationViewModel.firebaseUser}"
                            )
                            Toast.makeText(
                                context,
                                "The value for user is: ${applicationViewModel.currentUser}",
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
                    }
                }


            }
        }
    }


}

@Composable
fun CameraPhotos(
    modifier: Modifier,
    navController: NavHostController,
    allPhotos: SnapshotStateList<Photo>
) {
    Spacer(Modifier.height(8.dp))
    FlowRow(
        mainAxisSize = SizeMode.Expand,
        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween
    ) {
        val itemSize: Dp = (LocalConfiguration.current.screenWidthDp.dp / 2)
        PhotoCard(
            allPhotos,
            modifier = Modifier
                .size(itemSize - 12.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(5.dp))
                .aspectRatio(1f)
        )
        AddNewPhoto(
            modifier = Modifier
                .size(itemSize - 12.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(5.dp))
                .aspectRatio(1f),
            navController = navController
        )
    }

}

@Composable
fun PhotoCard(allPhotos: SnapshotStateList<Photo>, modifier: Modifier) {
    fun removePhoto(photo: Photo) {
        allPhotos.remove(photo)
    }
    allPhotos.forEach { photo ->
        Box(
            modifier = modifier
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(
                        data = photo.localUri
                    )
                    .placeholder(R.drawable.ic_image_placeholder)
                    .crossfade(true)
                    .build(),
                contentDescription = "",
                Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Scrim(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(Alignment.BottomCenter)
            )
            IconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                onClick = { removePhoto(photo) }) {
                Icon(Icons.Rounded.Delete, contentDescription = "Remove photo", tint = Color.White)
            }
        }
    }

}


@Composable
private fun InputRestaurantName(
    focusRequester: FocusRequester,
    addReviewViewModel: ReviewViewModel
) {
    OutlinedTextField(
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth(),
        label = {
            Text(text = "Restaurant name")
        },
        value = addReviewViewModel.restaurantNameText.value,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        onValueChange = { newValue ->
            addReviewViewModel.onTextFieldChange(
                addReviewViewModel.restaurantNameText,
                newValue
            )
        },
        textStyle = LocalTextStyle.current.copy(
            fontSize = MaterialTheme.typography.h6.fontSize,
            textAlign = TextAlign.Center
        )
    )
}


@Composable
fun ReviewTextfield(
    reviewViewModel: ReviewViewModel,
    modifier: Modifier,
    currentCharCount: MutableState<Int>
) {
    val context = LocalContext.current
    val maxChars = 1000

    Box(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            label = {
                Text(text = "(Optional) Write about it")
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
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
fun ReviewBar(
    value: MutableState<Int>,
    title: String,
    focusManager: FocusManager,
    icon: Int
) {
    Column(
        modifier = Modifier.padding(
            horizontal = MaterialTheme.spacing.medium,
            vertical = MaterialTheme.spacing.extraSmall
        )
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.scale(0.6f),
                painter = painterResource(id = icon),
                contentDescription = null
            )
            Text(
                text = title,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .border(2.dp, Color.Black, shape = RoundedCornerShape(25.dp))
                .padding(horizontal = MaterialTheme.spacing.small)
                .fillMaxWidth()
        ) {

            var sliderPosition by remember { mutableStateOf(value.value.toFloat()) }
            Slider(
                value = sliderPosition,
                onValueChange = {
                    focusManager.clearFocus()
                    sliderPosition = it
                },
                valueRange = 1f..3f,
                onValueChangeFinished = {
                    value.value = sliderPosition.toInt()
                    // launch some business logic update with the state you hold
                    // viewModel.updateSelectedSliderValue(sliderPosition)
                },
                steps = 1,
                colors = SliderDefaults.colors(
                    thumbColor = Color.Black,
                    activeTrackColor = Color.Black
                )
            )
        }
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
                reviewViewModel.getName()
                println("CURRENT NAME: ${reviewViewModel.displayName.value}")
                reviewViewModel.onSubmitButton(selectImages = cameraViewModel.selectImages)

                Toast.makeText(context, "Review saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    ) {
        Text("Submit")
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
            .border(1.dp, Color.Black, RoundedCornerShape(5.dp)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(3f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = reviewViewModel.address.value
            )
        }
        IconButton(
            modifier = Modifier.weight(1f),
            onClick = {
                focusManager.clearFocus()
                /*applicationViewModel.startLocationUpdates()*/
                reviewViewModel.changeLocation(
                    location!!.latitude,
                    location!!.longitude,
                    context = context
                )

            }) {
            Icon(
                Icons.Rounded.MyLocation,
                contentDescription = "My location"
            )
        }
        IconButton(
            modifier = Modifier.weight(1f),
            onClick = {
                focusManager.clearFocus()
                navController.navigate(Screen.FromAddChooseLocation.route)
            }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_outline_map),
                contentDescription = "Open map"
            )
        }
    }

}



