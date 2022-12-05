package com.example.gogieats.screens

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gogieats.R
import com.example.gogieats.Screen
import com.example.gogieats.data.firestore.EditingPost
import com.example.gogieats.data.photos.Photo
import com.example.gogieats.screens.camera.CameraViewModel
import com.example.gogieats.screens.map.location.LocationDetails
import com.example.gogieats.screens.profile.ProfileViewModel
import com.example.gogieats.screens.util.*
import com.example.gogieats.ui.theme.spacing
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode
import com.google.firebase.firestore.GeoPoint

@Composable
fun EditReview(
    navController: NavHostController,
    cameraViewModel: CameraViewModel,
    profileViewModel: ProfileViewModel,
    location: LocationDetails?,
) {
    BackHandler() {
        navController.navigate(Screen.Profile.route)
        profileViewModel.editingState.value = false
    }
    val focusManager = LocalFocusManager.current
    val focusRequester = FocusRequester()

    val photoList = profileViewModel.photoList
    val context = LocalContext.current

    val allPhotos = cameraViewModel.getAllPhotos()
    LaunchedEffect(key1 = Unit) {

        if (profileViewModel.editPhotoList.isNotEmpty()) {
            profileViewModel.editPhotoList.forEach {

                profileViewModel.photoList.add(it)
            }
            profileViewModel.editPhotoList.clear()
        }

        allPhotos.forEach {
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
            contentPadding = PaddingValues(
                top = 18.dp,
                bottom = 120.dp,
                start = 12.dp,
                end = 12.dp
            )
        ) {
            item {
                InputRestaurantName2(focusRequester = focusRequester, post = post)
            }
            item {
                Spacer(Modifier.height(24.dp))
            }
            item {
                EditAddress2(
                    profileViewModel,
                    location = location,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    post = post,
                    onClick = { focusManager.clearFocus() },
                    onNavigate = {
                        navController.navigate(
                            Screen.FromEditChooseLocation.route
                        )
                    }
                )
            }
            item {
                Spacer(Modifier.height(20.dp))
                ShadowDivider()
                Spacer(Modifier.height(10.dp))
            }
            item {
                Column(Modifier.fillMaxWidth()) {

                    CategoryCard(
                        modifier = Modifier.padding(vertical = 8.dp),
                        value = post.valueMeat,
                        title = R.string.title_meat,
                        icon = R.drawable.icon_meat,
                        description = R.string.description_meat
                    )
                    CategoryCard(
                        modifier = Modifier.padding(vertical = 8.dp),
                        value = post.valueSideDishes,
                        title = R.string.title_side_dishes,
                        icon = R.drawable.icon_side_dishes,
                        description = R.string.description_side_dishes

                    )
                    CategoryCard(
                        modifier = Modifier.padding(vertical = 8.dp),
                        value = post.valueAmenities,
                        title = R.string.title_amenities,
                        icon = R.drawable.icon_amenities,
                        description = R.string.description_amenities

                    )
                    CategoryCard(
                        modifier = Modifier.padding(vertical = 8.dp),
                        value = post.valueAtmosphere,
                        title = R.string.title_atmosphere,
                        icon = R.drawable.icon_atmosphere,
                        description = R.string.description_atmosphere

                    )


                }
                Spacer(modifier = Modifier.height(8.dp))
            }


            item {
                PhotoRow(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                    navController = navController,
                    allPhotos = photoList,
                    profileViewModel = profileViewModel
                )
            }
            item {

                val currentCharCount = remember { mutableStateOf(authorText.value.length) }
               /* ReviewCommentField(
                    authorText,
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                )*/
                WrittenReview(
                    modifier = Modifier.padding(vertical = 8.dp),
                    authorText = authorText,
                    currentCharCount = currentCharCount
                )
            }
            item {
                Spacer(Modifier.height(10.dp))
            }
            item {
                OrangeButton(modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.update),
                    onClick =
                    {
                        post.photoList = photoList
                        post.location = profileViewModel.changePostAddress()
                        profileViewModel.updateReview(
                            post.firebaseId,
                            post,
                            profileViewModel.editPhotoList
                        )
                        profileViewModel.editingState.value = false
                        Toast.makeText(context,
                            context.getString(R.string.post_updated),
                            Toast.LENGTH_SHORT).show()
                    })
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
                Text(text = stringResource(id = R.string.optional_write_about_it))
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
                    Toast.makeText(context,
                        context.getString(R.string.shorten_name),
                        Toast.LENGTH_SHORT).show()
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
        Text(stringResource(R.string.update))
    }
}

@Composable
fun PhotoGrid(
    photoList: SnapshotStateList<Photo>,
    modifier: Modifier,
    profileViewModel: ProfileViewModel,
    navController: NavHostController,
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
            Icon(Icons.Rounded.Camera, null)
        }

    }
}

@Composable
fun EditPhotoCard(
    photoList: SnapshotStateList<Photo>,
    profileViewModel: ProfileViewModel,
    modifier: Modifier,
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
            BlackScrim(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(Alignment.BottomCenter)
            )
            IconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                onClick = { removePhoto(photo) }) {
                Icon(Icons.Rounded.Delete,
                    contentDescription = stringResource(id = R.string.remove_photo),
                    tint = Color.White)
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
                Text(text = stringResource(id = R.string.restaurant_name))
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
    modifier: Modifier,
    post: EditingPost,
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
                post.location = GeoPoint(location!!.latitude, location.longitude)


            }) {
            Icon(
                Icons.Rounded.MyLocation,
                contentDescription = stringResource(id = R.string.my_location)
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
                contentDescription = stringResource(id = R.string.open_map)
            )
        }
    }
}
