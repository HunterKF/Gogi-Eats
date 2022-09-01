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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kbbqreview.Post
import com.example.kbbqreview.R
import com.example.kbbqreview.items
import com.example.kbbqreview.screens.login.LoadingScreen
import com.example.kbbqreview.screens.profile.ProfileScreenState
import com.example.kbbqreview.screens.profile.ProfileViewModel
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
                    ProfileContent(
                        posts = loaded.posts,
                        avatarUrl = loaded.avatarUrl,
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
fun ProfileContent(posts: List<Post>, avatarUrl: String, onSignOut: () -> Unit) {
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            UserBar(scope, sheetState)
            StatsBar()

            ViewSelector(gridLayout)
            if (gridLayout.value) {
                LazyVerticalGrid(columns = GridCells.Fixed(2), content = {
                    items(posts) { post ->
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.Green)
                        ) {
                            Text(text = "Item ${post.timestamp}")
                        }
                    }
                })
            } else {
                LazyColumn {
                    items(posts) { post ->
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.Green)
                        ) {
                            Text(text = "Item ${post.timestamp}")
                        }
                    }
                }
            }
        }
    }


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
private fun StatsBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reviews",
                style = MaterialTheme.typography.body1
            )
            Text(
                text = "7",
                style = MaterialTheme.typography.body1
            )
        }
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Locations")
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
    sheetState: BottomSheetState
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                modifier = Modifier
                    .scale(0.5f)
                    .align(Alignment.CenterStart)
                    .border(4.dp, Color.LightGray, CircleShape),
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Profile picture"
            )
        }

        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "User_ID",
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

/*
@Preview(showSystemUi = true)
@Composable
fun Preview() {
    ProfileScreen()
}*/
