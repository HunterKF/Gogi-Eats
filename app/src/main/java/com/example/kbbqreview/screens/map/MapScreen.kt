package com.example.kbbqreview

import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kbbqreview.data.firestore.Post
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.HomeScreen.HomePostCard
import com.example.kbbqreview.screens.addreview.ReviewViewModel
import com.example.kbbqreview.screens.map.MapStyle
import com.example.kbbqreview.screens.map.MapViewModel
import com.example.kbbqreview.screens.map.location.LocationDetails
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MapScreen(
    location: LocationDetails,
    navController: NavHostController,
    reviewViewModel: ReviewViewModel
) {

    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel = viewModel<MapViewModel>()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(location.latitude, location.longitude), 17f)
    }
    val uiSettings = remember {
        MapUiSettings(zoomControlsEnabled = false)
    }

    val posts = viewModel.observePosts().collectAsState(initial = emptyList())
    posts.value.forEach { post ->
        val result = FloatArray(10)
        Location.distanceBetween(
            post.location!!.latitude,
            post.location!!.longitude,
            location.latitude,
            location.longitude,
            result
        )
        val distanceInKilometers = viewModel.distanceInKm(result[0])
        post.distance = distanceInKilometers
    }
    val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )
    LaunchedEffect(key1 = Unit) {
        viewModel.newMarkerPositionLat.value = location!!.latitude
        viewModel.newMarkerPositionLng.value = location!!.longitude
    }
    var showSinglePost by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    Scaffold(
        floatingActionButton = {
            Column() {
                FloatingActionButton(onClick = {
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location.latitude,
                                location.longitude
                            ), 15f
                        )
                    )
                }) {
                    Icon(
                        Icons.Rounded.MyLocation,
                        contentDescription = "My Location"
                    )
                }
                Spacer(Modifier.size(4.dp))
                FloatingActionButton(onClick = {
                    scope.launch { sheetState.expand() }
                }) {
                    Icon(
                        Icons.Rounded.ViewList,
                        contentDescription = "List"
                    )
                }
            }
        },
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

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                SheetContent(scope, sheetState, posts, location, showSinglePost, viewModel)
            },
            sheetPeekHeight = 0.dp,
            sheetGesturesEnabled = true,
            sheetShape = RoundedCornerShape(topEnd = 5.dp, topStart = 5.dp)

        ) {
            GoogleMap(
                modifier = Modifier.padding(innerPadding),
                cameraPositionState = cameraPositionState,
                uiSettings = uiSettings,
                properties = MapProperties(
                    mapStyleOptions = MapStyleOptions(MapStyle.json)
                )
            ) {
                Marker(position = LatLng(location.latitude, location.longitude), flat = true)

                posts.value.forEach { post ->
                    MapMarker(
                        LocalContext.current,
                        LatLng(post.location!!.latitude, post.location.longitude),
                        post.restaurantName.value,
                        R.drawable.ic_baseline_star_rate_24,
                        post,
                        viewModel
                    ) {
                        showSinglePost = true
                        scope.launch { sheetState.expand() }
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
@Composable
private fun SheetContent(
    scope: CoroutineScope,
    sheetState: BottomSheetState,
    posts: State<List<Post>>,
    location: LocationDetails,
    showSinglePost: Boolean,
    viewModel: MapViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.TopCenter),
            onClick = { scope.launch { sheetState.collapse() } }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_more),
                contentDescription = "Close sheet"
            )
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 60.dp)) {
            if (showSinglePost) {
                item {
                    val state = rememberPagerState()
                    HomePostCard(state = state, post = viewModel.singlePost.value)
                }
            } else {
                itemsIndexed(posts.value.sortedByDescending { it.distance.toDouble() }
                    .reversed()) { index, restaurant ->
                    Spacer(Modifier.size(10.dp))
                    RestaurantCard(restaurant, location)
                    Spacer(Modifier.size(10.dp))
                    if (index != posts.value.size - 1) {
                        Divider(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantCard(restaurant: Post, currentLocation: LocationDetails) {
    val photoList by remember {
        mutableStateOf(restaurant.photoList)
    }
    val viewModel = MapViewModel()
    val location = viewModel.getAddressFromLocation(
        LocalContext.current,
        restaurant.location!!.latitude,
        restaurant.location.longitude
    )
    val emptyPhoto = Photo(
        "",
        "",
        "",
        0
    )
    Box {
        Column(Modifier.padding(8.dp)) {
            Box(Modifier.fillMaxWidth()) {
                PhotoDisplay(photoList)
            }
            Heading(restaurant)
            Distance(restaurant.location, currentLocation)
            Address(location, restaurant.location)
            ValueBar(restaurant)


        }
    }
}

@Composable
fun Distance(restaurantLocation: GeoPoint, currentLocation: LocationDetails) {
    val viewModel = MapViewModel()
    val result = FloatArray(10)
    Location.distanceBetween(
        restaurantLocation.latitude,
        restaurantLocation.longitude,
        currentLocation.latitude,
        currentLocation.longitude,
        result
    )
    val distanceInKilometers = viewModel.distanceInKm(result[0])

    Text(text = "${distanceInKilometers} km")
//    Text(text = "${result[0]} km")
}

@Composable
private fun PhotoDisplay(photoList: List<Photo>) {
    LazyRow(
        Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(photoList.take(5)) { photo ->
            Box {
                AsyncImage(
                    ImageRequest.Builder(LocalContext.current)
                        .data(photo.remoteUri)
                        .placeholder(R.drawable.ic_image_placeholder).crossfade(true)
                        .build(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .align(Alignment.Center),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun ValueBar(restaurant: Post) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        PointIcon(
            photo = R.drawable.meat_icon,
            value = restaurant.valueMeat.value
        )
        PointIcon(
            photo = R.drawable.side_dishes_icon,
            value = restaurant.valueSideDishes.value
        )
        PointIcon(
            photo = R.drawable.amenities_icon,
            value = restaurant.valueAmenities.value
        )
        PointIcon(
            photo = R.drawable.atmosphere_icon,
            value = restaurant.valueAtmosphere.value
        )
    }
}

@Composable
private fun Heading(restaurant: Post) {
    val totalValue =
        restaurant.valueAmenities.value + restaurant.valueMeat.value + restaurant.valueAtmosphere.value + restaurant.valueSideDishes.value
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(

            modifier = Modifier
                .padding(start = 8.dp)
                .border(1.dp, Color.Black, RoundedCornerShape(15.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.scale(0.75f),
                    painter = painterResource(id = R.drawable.ic_baseline_star_rate_24),
                    contentDescription = null
                )
                Text(totalValue.toString())
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = restaurant.restaurantName.value,
            style = MaterialTheme.typography.h6
        )
    }


}

@Composable
private fun Address(address: String, location: GeoPoint) {
    val context = LocalContext.current
    val mapIntent: Intent = Uri.parse(
        "geo:${location.latitude},${location.longitude}?z=8"
    ).let { location ->
        // Or map point based on latitude/longitude
        // val location: Uri = Uri.parse("geo:37.422219,-122.08364?z=14") // z param is zoom level
        Intent(Intent.ACTION_VIEW, location)
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = address,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Light
        )
        IconButton(onClick = { context.startActivity(mapIntent) }) {
            Icon(Icons.Rounded.Map, "Open map")
        }
    }

}


@Composable
fun PointIcon(photo: Int, value: Int) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.scale(0.5f),
            painter = painterResource(id = photo),
            contentDescription = null
        )
        Text("$value")
    }
}

@Composable
fun MapMarker(
    context: Context,
    position: LatLng,
    title: String,
    @DrawableRes iconResourceId: Int,
    post: Post,
    viewModel: MapViewModel,
    onInfoClick: () -> Unit
) {
    val viewModel = MapViewModel()
    val icon = viewModel.bitmapDescriptorFromVector(
        context, iconResourceId
    )
    Marker(
        position = position,
        title = title,
        icon = icon,
        snippet = "Hello",
        onInfoWindowClick = {
            onInfoClick()
            viewModel.singlePost.value = post
        }
    )
}



