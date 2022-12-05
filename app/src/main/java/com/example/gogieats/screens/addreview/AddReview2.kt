package com.example.gogieats.screens.addreview

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.gogieats.ApplicationViewModel
import com.example.gogieats.R
import com.example.gogieats.Screen
import com.example.gogieats.items
import com.example.gogieats.screens.camera.CameraViewModel
import com.example.gogieats.screens.map.location.LocationDetails
import com.example.gogieats.screens.util.*
import com.example.gogieats.ui.theme.*


@Composable
fun AddReview2(
    focusManager: FocusManager,
    navController: NavHostController,
    cameraViewModel: CameraViewModel,
    location: LocationDetails?,
    applicationViewModel: ApplicationViewModel,
    addReviewViewModel: ReviewViewModel,
) {
    val context = LocalContext.current
    val focusRequester = FocusRequester()
    val allPhotos = cameraViewModel.getAllPhotos()
    val currentCharCount = remember { mutableStateOf(0) }
    val lazyState = rememberLazyListState()

    addReviewViewModel.setDisplayName()

    LaunchedEffect(key1 = true, block = {
        if (addReviewViewModel.firebaseUser == null) {
            navController.navigate(Screen.Profile.route)
        }
    })

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var label = ""
    items.forEach {
        if (currentDestination?.route == it.route) {
            label = it.label
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .shadow(12.dp, RoundedCornerShape(0.dp), spotColor = Color.Black),
                backgroundColor = Color.White,
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.h6,
                            color = Brown
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(0.dp), spotColor = Color.Black),
                backgroundColor = Color.White
            ) {
                items.forEach { screen ->
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                modifier = Modifier.size(22.dp),
                                painter = painterResource(id = screen.icon
                                    ?: R.drawable.icon_meat),
                                contentDescription = null,
                                tint = if (currentDestination?.route == screen.route) Orange else Color.LightGray)
                        },
                        label = {
                            Text(
                                text = screen.label,
                                style = MaterialTheme.typography.subtitle2,
                                color = if (currentDestination?.route == screen.route) Orange else Color.LightGray)
                        },
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


        Column {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                /*.scrollable(state = lazyState, orientation = Orientation.Horizontal)*/,
                contentPadding = PaddingValues(
                    top = 18.dp,
                    bottom = 120.dp,
                    start = 12.dp,
                    end = 12.dp
                )
            ) {
                item {
                    InputRestaurantName2(
                        focusRequester = focusRequester,
                        addReviewViewModel = addReviewViewModel
                    )
                }
                item {
                    Spacer(Modifier.height(24.dp))
                }
                item {
                    AddressBar(
                        applicationViewModel = applicationViewModel,
                        reviewViewModel = addReviewViewModel,
                        location = location,
                        navController = navController,
                        focusManager = focusManager,
                    )
                }
                item {
                    Spacer(Modifier.height(20.dp))
                    ShadowDivider()
                    Spacer(Modifier.height(10.dp))
                }
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val mutableState = remember {
                            mutableStateOf(2)
                        }
                        Text(
                            text = "Rating",
                            style = MaterialTheme.typography.h6,
                            color = Brown,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        CategoryCard(modifier = Modifier.padding(vertical = 8.dp),
                            value = addReviewViewModel.valueMeat,
                            title = R.string.title_meat,
                            icon = R.drawable.icon_meat,
                            description = R.string.description_meat)
                        CategoryCard(value = addReviewViewModel.valueSideDishes,
                            title = R.string.title_side_dishes,
                            icon = R.drawable.icon_side_dishes,
                            description = R.string.description_side_dishes,
                            modifier = Modifier.padding(vertical = 8.dp))
                        CategoryCard(value = addReviewViewModel.valueAmenities,
                            title = R.string.title_amenities,
                            icon = R.drawable.icon_amenities,
                            description = R.string.description_amenities,
                            modifier = Modifier.padding(vertical = 8.dp))
                        CategoryCard(value = addReviewViewModel.valueAtmosphere,
                            title = R.string.title_atmosphere,
                            icon = R.drawable.icon_atmosphere,
                            description = R.string.description_atmosphere,
                            modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                item {
                    PhotoRow(modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                        navController = navController,
                        allPhotos = allPhotos)
                }
                item {
                    WrittenReview(
                        reviewViewModel = addReviewViewModel,
                        modifier = Modifier.padding(vertical = 8.dp),
                        currentCharCount = currentCharCount
                    )
                }
                item {
                    Spacer(Modifier.height(10.dp))
                }
                item {
                    OrangeButton(text = stringResource(id = R.string.submit),
                        onClick = {  when {
                            addReviewViewModel.restaurantLng.value == 0.0 && addReviewViewModel.restaurantLat.value == 0.0 -> {
                                Toast.makeText(context, context.getString(R.string.check_location), Toast.LENGTH_SHORT).show()
                            }
                            cameraViewModel.selectImages.isEmpty() -> {
                                Toast.makeText(context,  context.getString(R.string.check_photo), Toast.LENGTH_SHORT).show()
                            }
                            addReviewViewModel.restaurantNameText.value == "" -> {
                                Toast.makeText(context,  context.getString(R.string.check_name), Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                addReviewViewModel.onSubmitButton(selectImages = cameraViewModel.selectImages)

                                Toast.makeText(context,  context.getString(R.string.review_saved), Toast.LENGTH_SHORT).show()
                            }
                        } },
                        modifier = Modifier.fillMaxWidth())
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