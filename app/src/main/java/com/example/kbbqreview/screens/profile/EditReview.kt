package com.example.kbbqreview.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kbbqreview.R
import com.example.kbbqreview.Screen
import com.example.kbbqreview.data.firestore.EditingPost
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.camera.CameraViewModel
import com.example.kbbqreview.screens.map.location.LocationDetails
import com.example.kbbqreview.screens.profile.ProfileViewModel
import com.example.kbbqreview.ui.theme.spacing
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode

@Composable
fun EditReview(
    navController: NavHostController,
    cameraViewModel: CameraViewModel,
    profileViewModel: ProfileViewModel,
    location: LocationDetails?
) {
    BackHandler() {
        navController.navigate(Screen.Profile.route)
        profileViewModel.editingState.value = false
    }
    val focusManager = LocalFocusManager.current
    val photoList = profileViewModel.photoList
    val context = LocalContext.current

    val allPhotos = cameraViewModel.getAllPhotos()
    LaunchedEffect(key1 = Unit) {
        Log.d(
            "LaunchedEffect",
            "LAUNCHED EFFECT HAS HAPPENED!!! HOLD YOUR SHIT, IT'S ABOUT TO GET LOADED!"
        )
        if (profileViewModel.editPhotoList.isNotEmpty()) {
            profileViewModel.editPhotoList.forEach {
                Log.d(
                    "LaunchedEffect",
                    "in editPhotoList.forEach -> allPhotos size: ${profileViewModel.editPhotoList.size}"
                )
                Log.d(
                    "LaunchedEffect",
                    "in editPhotoList.forEach -> photoList size: ${photoList.size}"
                )
                profileViewModel.photoList.add(it)
            }
            profileViewModel.editPhotoList.clear()
        }

        allPhotos.forEach {
            Log.d("LaunchedEffect", "in allPhotos.forEach -> allPhotos size: ${allPhotos.size}")
            Log.d("LaunchedEffect", "in allPhotos.forEach -> photoList size: ${photoList.size}")
            if (photoList.contains(it)) {
                return@forEach
            } else {
                profileViewModel.photoList.add(it)
            }
        }
        allPhotos.clear()
        profileViewModel.editPhotoList.clear()
    }
    val post = remember {
        profileViewModel.editingPost
    }

    val authorText = remember {
        post.authorText
    }


    Surface() {
        LazyColumn(
            Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            item {
                TopBar(
                    post,
                    modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.small),
                    profileViewModel.restaurantName
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
                EditAddress(
                    profileViewModel,
                    location = location,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = { focusManager.clearFocus() },
                    onNavigate = {
                        navController.navigate(
                            Screen.FromEditChooseLocation.route
                        )
                    })
            }
            item {
                Column(Modifier.padding(horizontal = 12.dp)) {

                    ReviewScale(post.valueMeat, "Meat", focusManager, R.drawable.meat_icon)
                    ReviewScale(
                        post.valueSideDishes,
                        "Side Dishes",
                        focusManager,
                        R.drawable.side_dishes_icon
                    )

                    ReviewScale(
                        post.valueAmenities,
                        "Amenities",
                        focusManager,
                        R.drawable.amenities_icon
                    )
                    ReviewScale(
                        post.valueAtmosphere,
                        "Atmosphere",
                        focusManager,
                        R.drawable.atmosphere_icon
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }


            item {
                PhotoGrid(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    photoList = photoList,
                    navController = navController,
                    profileViewModel = profileViewModel
                )
            }
            item {
                ReviewCommentField(
                    authorText,
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                )
            }
            item {
                UpdateButton(modifier = Modifier.fillMaxWidth()) {
                    post.photoList = photoList
                    profileViewModel.updateReview(
                        post.firebaseId,
                        post,
                        profileViewModel.editPhotoList
                    )
                    profileViewModel.editingState.value = false
                    Toast.makeText(context, "Post updated.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}

@Composable
fun ReviewCommentField(authorText: MutableState<String>, modifier: Modifier) {
    val context = LocalContext.current
    val currentCharCount = remember { mutableStateOf(authorText.value.length) }
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
            value = authorText.value,
            onValueChange = { newValue ->
                if (newValue.length <= maxChars) {
                    currentCharCount.value = newValue.length
                    authorText.value = newValue
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
fun ReviewScale(value: MutableState<Int>, title: String, focusManager: FocusManager, icon: Int) {
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
                .border(2.dp, Color.Black, RoundedCornerShape(25.dp))
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
fun UpdateButton(modifier: Modifier, onUpdate: () -> Unit) {
    OutlinedButton(modifier = modifier, onClick = { onUpdate() }) {
        Text("Update")
    }
}

@Composable
fun PhotoGrid(
    photoList: SnapshotStateList<Photo>,
    modifier: Modifier,
    profileViewModel: ProfileViewModel,
    navController: NavHostController
) {
    FlowRow(
        mainAxisSize = SizeMode.Expand,
        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween
    ) {
        val itemSize: Dp = (LocalConfiguration.current.screenWidthDp.dp / 2)
        EditPhotoCard(
            photoList, modifier = Modifier
                .size(itemSize)
                .padding(8.dp)
                .clip(RoundedCornerShape(5.dp))
                .aspectRatio(1f),
            profileViewModel = profileViewModel
        )
        AddNewPhoto(
            modifier = Modifier
                .size(itemSize)
                .padding(8.dp)
                .clip(RoundedCornerShape(5.dp))
                .aspectRatio(1f),
            navController = navController
        )
    }
}


@Composable
fun AddNewPhoto(modifier: Modifier, navController: NavHostController) {
    Box(
        modifier = modifier
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = { navController.navigate(Screen.MainCamera.route) }) {
            Icon(Icons.Rounded.Camera, "Open camera")
        }

    }
}

@Composable
fun EditPhotoCard(
    photoList: SnapshotStateList<Photo>,
    profileViewModel: ProfileViewModel,
    modifier: Modifier
) {
    fun removePhoto(photo: Photo) {
        photoList.remove(photo)
        profileViewModel.addPhotoToBeDeleted(photo)
    }

    var listIndex = 0
    photoList.forEach { photo ->
        photo.listIndex = listIndex
        listIndex += 1
        var uri = photo.remoteUri
        if (uri == "") {
            uri = photo.localUri
        }
        Box(
            modifier = modifier
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(
                        data = uri
                    )
                    .placeholder(R.drawable.ic_image_placeholder)
                    .crossfade(true)
                    .build(),
                contentDescription = "",
                Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            com.example.kbbqreview.screens.HomeScreen.Scrim(
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
fun TopBar(post: EditingPost, modifier: Modifier, restaurantName: MutableState<String>) {
    var textFieldState by remember {
        mutableStateOf("")
    }
    val totalValue =
        post.valueAmenities.value + post.valueMeat.value + post.valueAtmosphere.value + post.valueSideDishes.value

    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedTextField(
            modifier = Modifier

                .fillMaxWidth(),
            label = {
                Text(text = "Restaurant name")
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            value = post.restaurantName.value,
            onValueChange = { newValue -> post.restaurantName.value = newValue },
            textStyle = LocalTextStyle.current.copy(
                fontSize = MaterialTheme.typography.h6.fontSize,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun EditAddress(
    profileViewModel: ProfileViewModel,
    location: LocationDetails?,
    onClick: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black, RoundedCornerShape(5.dp)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(3f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = profileViewModel.address.value
            )
        }
        IconButton(
            modifier = Modifier.weight(1f),
            onClick = {
                onClick()
                profileViewModel.changeLocation(
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
                onClick()
                onNavigate()
            }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_outline_map),
                contentDescription = "Open map"
            )
        }
    }
}
