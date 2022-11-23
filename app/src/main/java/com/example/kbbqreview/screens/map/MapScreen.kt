package com.example.kbbqreview

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kbbqreview.data.firestore.Post
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.HomeScreen.HomePostCard
import com.example.kbbqreview.screens.map.MapStyle
import com.example.kbbqreview.screens.map.MapViewModel
import com.example.kbbqreview.screens.map.location.LocationDetails
import com.example.kbbqreview.ui.theme.Brown
import com.example.kbbqreview.ui.theme.Orange
import com.example.kbbqreview.util.BitmapHandler
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    location: LocationDetails,
    navController: NavHostController,
    mapViewModel: MapViewModel,
    posts: State<List<Post>>,
) {

    val permissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(
                mapViewModel.newMarkerPositionLat.value,
                mapViewModel.newMarkerPositionLng.value
            ), 15f)
    }
    val uiSettings = remember {
        MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false)
    }

    posts.value.forEach { post ->
        val result = FloatArray(10)
        Location.distanceBetween(
            post.location!!.latitude,
            post.location!!.longitude,
            location.latitude,
            location.longitude,
            result
        )
        val distanceInKilometers = mapViewModel.distanceInKm(result[0])
        post.distance = distanceInKilometers
    }
    val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )
    var showSinglePost = remember {
        mutableStateOf(false)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var label = ""
    items.forEach {
        if (currentDestination?.route == it.route) {
            label = it.label
        }
    }
    val scope = rememberCoroutineScope()
    var expand by remember {
        mutableStateOf(false)
    }

    when {
        sheetState.isExpanded -> expand = true
        sheetState.isCollapsed -> expand = false
    }
    val offsetAnimation: Dp by animateDpAsState(
        if (expand) 100.dp else 0.dp,
        tween(200)
    )
    val offsetSizeAnimation: Float by animateFloatAsState(
        if (expand) 0.4f else 1.0f,
        spring(dampingRatio = Spring.DampingRatioLowBouncy)
    )
    val mapSizeAnimation: Float by animateFloatAsState(
        if (expand) 0.4f else 1.0f,
        tween(200)
    )
    when {
        permissionState.hasPermission -> {
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
                floatingActionButton = {
                    Column() {
                        FloatingActionButton(
                            modifier = Modifier
                                .offset(offsetAnimation)
                                .scale(offsetSizeAnimation),
                            onClick = {
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
                                modifier = Modifier.scale(offsetSizeAnimation),
                                contentDescription = "My Location"
                            )
                        }
                        Spacer(Modifier.size(4.dp))
                        FloatingActionButton(
                            modifier = Modifier
                                .offset(offsetAnimation)
                                .scale(offsetSizeAnimation),
                            onClick = {
                                scope.launch {
                                    expand = true
                                    sheetState.expand()
                                }
                            }) {
                            Icon(
                                Icons.Rounded.ViewList,
                                modifier = Modifier.scale(offsetSizeAnimation),
                                contentDescription = "List"
                            )
                        }
                    }
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


                if (sheetState.isAnimationRunning) {
                    println(sheetState.currentValue)
                    println(sheetState.progress)
                    println(sheetState.direction)
                }
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetContent = {
                        SheetContent(scope, sheetState, posts, location, showSinglePost, mapViewModel)
                    },
                    sheetPeekHeight = 0.dp,
                    sheetGesturesEnabled = true,
                    sheetShape = RoundedCornerShape(topEnd = 10.dp, topStart = 10.dp)

                ) {
                    GoogleMap(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxHeight(mapSizeAnimation)
                            .fillMaxWidth(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = uiSettings,
                        properties = MapProperties(
                            mapStyleOptions = MapStyleOptions(MapStyle.json)
                        ),
                        onMapClick = { showSinglePost.value = false}
                    ) {
                        Marker(position = LatLng(location.latitude, location.longitude), flat = true)

                        posts.value.forEach { post ->
                            val total = post.valueMeat + post.valueAmenities + post.valueAtmosphere + post.valueSideDishes
                            MapMarker(
                                LocalContext.current,
                                LatLng(post.location!!.latitude, post.location.longitude),
                                post.restaurantName,
                                R.drawable.map_marker__2_,
                                total = total
                            ) {
                                showSinglePost.value = true
                                mapViewModel.singlePost.value = post
                                scope.launch { sheetState.expand() }
                            }
                        }
                    }
                }
            }
        }
        permissionState.shouldShowRationale -> {
            Column() {
                Text(stringResource(R.string.location_permission_required))
            }
        }
        !permissionState.hasPermission && !permissionState.shouldShowRationale -> {
            Toast.makeText(context, stringResource(R.string.prompt_location_permission), Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.HomeScreen.route)
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
    showSinglePost: MutableState<Boolean>,
    viewModel: MapViewModel,
) {

    var expand by remember {
        mutableStateOf(false)
    }
    val offsetAnimation: Float by animateFloatAsState(
        if (expand) 1.0f else 0.8f,
        tween(200)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(offsetAnimation)
    ) {

        val state = rememberLazyListState()

        LazyColumn(contentPadding = PaddingValues(top = 15.dp, bottom = 60.dp), state = state) {
            when {
                (state.firstVisibleItemScrollOffset >= 3) -> expand = true
                sheetState.isCollapsed -> expand = false

            }
           item {
               Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                   IconButton(
                       onClick = { scope.launch { if (sheetState.isCollapsed) sheetState.expand() else sheetState.collapse() } }) {
                       Icon(
                           Icons.Rounded.HorizontalRule,
                           contentDescription = null,
                           modifier = Modifier
                               .scale(1.5f)
                               .offset(y = (-10).dp),
                           tint = Color.LightGray
                       )
                   }
               }
           }
            if (showSinglePost.value) {
                item {
                    val state = rememberPagerState()
                    HomePostCard(state = state,
                        post = viewModel.singlePost.value,
                        photoList = viewModel.singlePost.value.photoList)
                }
            } else {
                itemsIndexed(posts.value.sortedByDescending { it.distance.toDouble() }
                    .reversed()) { index, restaurant ->
                    Spacer(Modifier.size(10.dp))
                    RestaurantCard(restaurant, location) {
                        viewModel.singlePost.value = restaurant
                        showSinglePost.value = true
                    }
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
fun RestaurantCard(restaurant: Post, currentLocation: LocationDetails, onClick: () -> Unit) {
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
            Heading(restaurant, onClick)
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
            value = restaurant.valueMeat
        )
        PointIcon(
            photo = R.drawable.side_dishes_icon,
            value = restaurant.valueSideDishes
        )
        PointIcon(
            photo = R.drawable.amenities_icon,
            value = restaurant.valueAmenities
        )
        PointIcon(
            photo = R.drawable.atmosphere_icon,
            value = restaurant.valueAtmosphere
        )
    }
}

@Composable
private fun Heading(restaurant: Post, onClick: () -> Unit) {
    val totalValue =
        restaurant.valueAmenities + restaurant.valueMeat + restaurant.valueAtmosphere + restaurant.valueSideDishes
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
            text = restaurant.restaurantName,
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
            Icon(Icons.Rounded.Map, stringResource(id = R.string.open_map))
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
    total: Int,
    onInfoClick: () -> Unit,
) {
    val icon = BitmapHandler.bitmapDescriptorFromVector(
        context, iconResourceId
    )
    fun getEmojiByUnicode(unicode: Int): String {
        return  String(Character.toChars(unicode));
    }
    val text = "${getEmojiByUnicode(0x2B50)} $total"
    Marker(
        position = position,
        title = title,
        icon = icon,
        snippet = text,
        onInfoWindowClick = {
            onInfoClick()

        }
    )
}



