package com.example.kbbqreview.screens.addreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.kbbqreview.ApplicationViewModel
import com.example.kbbqreview.R
import com.example.kbbqreview.screens.camera.CameraViewModel
import com.example.kbbqreview.screens.map.location.LocationDetails
import com.example.kbbqreview.screens.util.*
import com.example.kbbqreview.ui.theme.OffWhite
import com.example.kbbqreview.ui.theme.Shadows
import com.example.kbbqreview.ui.theme.Yellow


@Composable
fun AddReview2(
    focusManager: FocusManager,
    navController: NavHostController,
    cameraViewModel: CameraViewModel,
    location: LocationDetails?,
    applicationViewModel: ApplicationViewModel,
    addReviewViewModel: ReviewViewModel
) {
    val focusRequester = FocusRequester()
    val allPhotos = cameraViewModel.getAllPhotos()
    val currentCharCount = remember { mutableStateOf(0) }
    Column {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(0.dp), spotColor = Color.Black),
            backgroundColor = Color.White,
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Review"
                    )
                }
            }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp)
        ) {
            item {
                InputRestaurantName2(
                    focusRequester = focusRequester,
                    /*addReviewViewModel = addReviewViewModel*/
                )
            }
            item {
                Spacer(Modifier.height(24.dp))
            }
            item {
                AddressBar()
            }
            item{
                Spacer(Modifier.height(20.dp))
            }
            item {
                ShadowDivider()
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val mutableState = remember {
                        mutableStateOf(2)
                    }
                    Text(
                        text = "Rating"
                    )
                    CategoryCard(modifier = Modifier.padding(vertical = 8.dp), value = mutableState, title = R.string.title_meat, icon = R.drawable.meat_icon, description = R.string.description_meat)
                    CategoryCard(value = mutableState,
                        title = R.string.title_side_dishes,
                        icon = R.drawable.side_dishes_icon,
                        description = R.string.description_side_dishes,
                        modifier = Modifier.padding(vertical = 8.dp))
                    CategoryCard(value = mutableState,
                        title = R.string.title_amenities,
                        icon = R.drawable.amenities_icon,
                        description = R.string.description_amenities,
                        modifier = Modifier.padding(vertical = 8.dp))
                    CategoryCard(value = mutableState,
                        title = R.string.title_atmosphere,
                        icon = R.drawable.atmosphere_icon,
                        description = R.string.description_atmosphere,
                        modifier = Modifier.padding(vertical = 8.dp))
                }
            }
            
            item {
                PhotoRow(modifier = Modifier.padding(2.dp), navController = navController, allPhotos = allPhotos)
            }
            item {
                WrittenReview(reviewViewModel = addReviewViewModel, currentCharCount = currentCharCount)
            }
            item {
                OrangeButton(text = stringResource(id = R.string.submit), onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth())
            }

        }

    }

}

@Composable
private fun InputRestaurantName2(
    focusRequester: FocusRequester,
    /*addReviewViewModel: ReviewViewModel*/
) {
    val value = remember {
        mutableStateOf("")
    }
    TextField(
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .shadow(Shadows().small,
                Shapes().medium,
                spotColor = Color.DarkGray,
                ambientColor = Color.Transparent),
        label = {
            Text(text = stringResource(R.string.restaurant_name))
        },
        value = value.value,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        onValueChange = { newValue ->
            value.value = newValue
        },
        colors = TextFieldDefaults.textFieldColors(
            textColor = Color.LightGray,
            disabledTextColor = Color.Transparent,
            backgroundColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        textStyle = LocalTextStyle.current.copy(
            fontSize = MaterialTheme.typography.h6.fontSize,
            textAlign = TextAlign.Center
        )
    )
}



@Composable
fun AddressBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3.5f)
            .shadow(Shadows().small,
                Shapes().medium,
                spotColor = Color.DarkGray,
                ambientColor = Color.Transparent)
            .background(Color.White),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                "Address here"
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.End
            ) {
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        /*focusManager.clearFocus()
                        *//*applicationViewModel.startLocationUpdates()*//*
                    reviewViewModel.changeLocation(
                        location!!.latitude,
                        location!!.longitude,
                        context = context
                    )*/

                    }) {
                    Icon(
                        Icons.Rounded.MyLocation,
                        contentDescription = stringResource(R.string.my_location),
                        tint = Yellow
                    )
                }
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        /* focusManager.clearFocus()
                         navController.navigate(Screen.FromAddChooseLocation.route)*/
                    }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_outline_map),
                        contentDescription = stringResource(R.string.open_map),
                        tint = Yellow
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun Preview4() {
    Surface(modifier = Modifier.fillMaxSize(), color = OffWhite) {
//        AddReview2()
    }
}