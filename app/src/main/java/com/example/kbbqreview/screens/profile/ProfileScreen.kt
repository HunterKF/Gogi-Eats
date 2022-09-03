package com.example.kbbqreview.screens.HomeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kbbqreview.Post
import com.example.kbbqreview.R
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.items
import com.example.kbbqreview.screens.login.LoadingScreen
import com.example.kbbqreview.screens.profile.ProfileScreenState
import com.example.kbbqreview.screens.profile.ProfileViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavHostController, navigationToSignIn: () -> Unit) {


    val profileViewModel = ProfileViewModel()
    val state by profileViewModel.state.collectAsState()
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
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                is ProfileScreenState.Loaded -> {
                    val loaded = state as ProfileScreenState.Loaded
                    val displayName = profileViewModel.setDisplayName()
                    val avatarUrl = profileViewModel.setAvatar()
                    ProfileContent(
                        posts = loaded.posts,
                        avatarUrl = avatarUrl,
                        displayName = displayName,
                        onSignOut = {
                            profileViewModel.signOut()
                        }
                    )
                }

                ProfileScreenState.Loading -> LoadingScreen()
                ProfileScreenState.SignInRequired -> LaunchedEffect(key1 = Unit) {
                    navigationToSignIn()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProfileContent(
    posts: List<Post>,
    avatarUrl: String,
    onSignOut: () -> Unit,
    displayName: String
) {
    val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    val gridLayout = remember {
        mutableStateOf(true)
    }
    val scope = rememberCoroutineScope()
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.TopCenter),
                    onClick = { scope.launch { sheetState.collapse() } }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_more),
                        contentDescription = "Close sheet"
                    )
                }

                Column(modifier = Modifier.align(Alignment.Center)) {
                    Row() {
                        Text("Sign Out")
                        IconButton(onClick = {
                            onSignOut()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_outline_cancel),
                                tint = Color.Red,
                                contentDescription = "Sign out"
                            )
                        }
                    }
                }
            }
        },
        sheetPeekHeight = 0.dp,
        sheetGesturesEnabled = true,
        sheetShape = RoundedCornerShape(topEnd = 5.dp, topStart = 5.dp)

    ) {
        val size = posts.size
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            UserBar(scope, sheetState, displayName, avatarUrl)
            StatsBar(size)
            ViewSelector(gridLayout)
            if (gridLayout.value) {
                LazyVerticalGrid(columns = GridCells.Fixed(2), content = {
                    items(posts) { post ->
                        GridViewCard(post)
                    }
                })
            } else {
                LazyColumn {
                    items(posts) { post ->
                        SingleViewCard(post)
                    }
                }
            }
        }
    }


}

@Composable
private fun GridViewCard(post: Post) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(5.dp))
            .aspectRatio(1f)
    ) {
        val photoList = post.photoList
        val imageUrl = remember {
            mutableStateOf(
                Photo(
                    "",
                    "",
                    "",
                    0
                )

            )
        }
        if (post.photoList.isNotEmpty()) {
            imageUrl.value = post.photoList[0]
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(data = imageUrl.value.remoteUri)
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
        Text(
            modifier = Modifier
                .padding(6.dp)
                .align(Alignment.BottomStart),
            text = post.restaurantName,
            color = MaterialTheme.colors.onSurface
        )
    }
}


@OptIn(ExperimentalPagerApi::class)
@Composable
private fun SingleViewCard(post: Post) {
    val state = rememberPagerState()
    PostCard(post = post, state = state)
}

@Composable
private fun ViewSelector(gridLayout: MutableState<Boolean>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { gridLayout.value = true }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_grid_view_24),
                contentDescription = "Grid view"
            )
        }
        IconButton(onClick = { gridLayout.value = false }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_crop_din_24),
                contentDescription = "Single view"
            )
        }
    }
}

@Composable
private fun StatsBar(size: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reviews",
                style = MaterialTheme.typography.body1
            )
            Text("Locations")

        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(size.toString())
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outline_map),
                    contentDescription = "Open map of location"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun UserBar(
    scope: CoroutineScope,
    sheetState: BottomSheetState,
    userName: String,
    avatarUrl: String,
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(modifier = Modifier.padding(8.dp), onClick = { /*TODO*/ }) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .placeholder(R.drawable.profile)
                    .crossfade(true)
                    .build(), contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Blue, CircleShape)
            )/*
            Icon(
                modifier = Modifier
                    .scale(0.5f)
                    .align(Alignment.CenterStart)
                    .border(4.dp, Color.LightGray, CircleShape),
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Profile picture"
            )*/
        }

        Text(
            modifier = Modifier.align(Alignment.Center),
            text = userName,
            style = MaterialTheme.typography.h6
        )
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = { scope.launch { if (sheetState.isCollapsed) sheetState.expand() else sheetState.collapse() } }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_more),
                contentDescription = "Options"
            )
        }
    }
}

@Composable
fun Scrim(modifier: Modifier) {
    //This is to make the bottom of the photo cards a little more readable
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                listOf(Color.Transparent, Color(0x99000000))
            )
        )
    )
}