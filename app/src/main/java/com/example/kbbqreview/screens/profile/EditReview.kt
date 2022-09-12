package com.example.kbbqreview.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.kbbqreview.R
import com.example.kbbqreview.Screen
import com.example.kbbqreview.data.firestore.Post
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.camera.CameraViewModel
import com.example.kbbqreview.ui.theme.spacing
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode

@Composable
fun EditReview(post: Post, navController: NavHostController, cameraViewModel: CameraViewModel) {
    val focusManager = LocalFocusManager.current
    val photoList = remember {
        mutableStateListOf<Photo>()
    }
    LaunchedEffect(key1 = post.photoList) {
        post.photoList.forEach {
                photo ->
            photoList.add(photo)
        }
    }
    val valueMeat = remember {
        mutableStateOf(post.valueMeat)
    }
    val valueSideDishes = remember {
        mutableStateOf(post.valueSideDishes)
    }
    val valueAmenities = remember {
        mutableStateOf(post.valueAmenities)
    }
    val valueAtmosphere = remember {
        mutableStateOf(post.valueAtmosphere)
    }
    val authorText = remember {
        mutableStateOf(post.authorText)
    }
    val allPhotos = cameraViewModel.getAllPhotos()
    allPhotos.forEach {
        if (photoList.contains(it)) {
            println("Skipping this photo.")
        } else {
            photoList.add(it)
            println("Photo added.")
        }
    }
    Surface() {
        LazyColumn(
            Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { TopBar(post, modifier = Modifier.fillMaxWidth()) }
            item { EditAddress(post) { focusManager.clearFocus() } }
            item { ReviewScale(valueMeat, "Meat", focusManager, R.drawable.meat_icon) }
            item {
                ReviewScale(
                    valueSideDishes,
                    "Side Dishes",
                    focusManager,
                    R.drawable.side_dishes_icon
                )
            }
            item {
                ReviewScale(
                    valueAmenities,
                    "Amenities",
                    focusManager,
                    R.drawable.amenities_icon
                )
            }
            item {
                ReviewScale(
                    valueAtmosphere,
                    "Atmosphere",
                    focusManager,
                    R.drawable.atmosphere_icon
                )
            }

            item {
                PhotoGrid(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    photoList = photoList,
                    navController = navController
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
            item { UpdateButton(modifier = Modifier.fillMaxWidth()) }
        }
    }


}

@Composable
fun ReviewCommentField(authorText: MutableState<String>, modifier: Modifier) {
    val context = LocalContext.current
    val currentCharCount = remember { mutableStateOf(authorText.value.length) }
    val maxChars = 1000

    Text(text = "(Optional) Write about it", style = MaterialTheme.typography.subtitle1)
    Box(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
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
            horizontal = MaterialTheme.spacing.small,
            vertical = MaterialTheme.spacing.extraSmall
        )
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.scale(0.5f),
                painter = painterResource(id = icon),
                contentDescription = null
            )
            Text(
                text = title,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.small)
                .fillMaxWidth()
                .border(1.dp, Color.Black, RoundedCornerShape(20.dp))
        ) {

            var sliderPosition by remember { mutableStateOf(value.value.toFloat()) }
            Slider(
                value = sliderPosition,
                onValueChange = {
                    focusManager.clearFocus()
                    sliderPosition = it
                    println(value.value)
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
fun UpdateButton(modifier: Modifier) {
    OutlinedButton(modifier = modifier, onClick = { /*TODO*/ }) {
        Text("Update")
    }
}

@Composable
fun PhotoGrid(
    photoList: SnapshotStateList<Photo>,
    modifier: Modifier,
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
                .aspectRatio(1f)
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
        IconButton(onClick = { navController.navigate(Screen.MainContentCamera.route) }) {
            Icon(Icons.Rounded.Camera, "Open camera")
        }

    }
}

@Composable
fun EditPhotoCard(
    photoList: SnapshotStateList<Photo>,
    modifier: Modifier
) {
    fun removePhoto(photo: Photo) {
        photoList.remove(photo)
    }
    photoList.forEach { photo ->
        Box(
            modifier = modifier
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(
                        data = photo.remoteUri
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
fun TopBar(post: Post, modifier: Modifier) {
    var textFieldState by remember {
        mutableStateOf("")
    }
    val totalValue =
        post.valueAmenities + post.valueMeat + post.valueAtmosphere + post.valueSideDishes

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(modifier = Modifier,
            onClick = { /*TODO delete*/ }) {
            Icon(Icons.Rounded.MoreHoriz, null)
        }
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = post.restaurantName,
            onValueChange = { newValue -> post.restaurantName = newValue },
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
        )
        IconButton(modifier = Modifier,
            onClick = { /*TODO delete*/ }) {
            Icon(Icons.Rounded.MoreHoriz, null)
        }
    }
}

@Composable
fun EditAddress(post: Post, onClick: () -> Unit) {
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
            Text(text = "reviewViewModel.address.value")
        }
        IconButton(
            modifier = Modifier.weight(1f),
            onClick = {
                onClick()
                /*applicationViewModel.startLocationUpdates()*/
                /*reviewViewModel.changeLocation(
                    location!!.latitude,
                    location!!.longitude,
                    context = context
                )*/

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
//                navController.navigate(Screen.ChooseLocationMap.route)
            }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_outline_map),
                contentDescription = "Open map"
            )
        }
    }
}
