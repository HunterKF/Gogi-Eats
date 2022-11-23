package com.example.kbbqreview.screens.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.kbbqreview.ApplicationViewModel
import com.example.kbbqreview.R
import com.example.kbbqreview.Screen
import com.example.kbbqreview.screens.addreview.ReviewViewModel
import com.example.kbbqreview.screens.map.location.LocationDetails
import com.example.kbbqreview.ui.theme.Shadows
import com.example.kbbqreview.ui.theme.Yellow

@Composable
fun AddressBar(
    applicationViewModel: ApplicationViewModel,
    reviewViewModel: ReviewViewModel,
    location: LocationDetails?,
    navController: NavHostController,
    focusManager: FocusManager,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f)
            .shadow(Shadows().small,
                Shapes().medium,
                spotColor = Color.Gray,
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
                modifier = Modifier.padding(end = 38.dp),
                text = reviewViewModel.address.value,
                style = MaterialTheme.typography.subtitle1,
                color = if (reviewViewModel.address.value == "Address here") Color.Gray else Color.DarkGray,
                fontSize = if (reviewViewModel.address.value == "Address here") 16.sp else 24.sp
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
                        focusManager.clearFocus()
                        applicationViewModel.startLocationUpdates()
                        reviewViewModel.changeLocation(
                            location!!.latitude,
                            location!!.longitude,
                            context = context
                        )

                    }) {
                    Icon(
                        Icons.Rounded.MyLocation,
                        contentDescription = stringResource(R.string.my_location),
                        tint = Yellow
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        focusManager.clearFocus()
                        navController.navigate(Screen.FromAddChooseLocation.route)
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