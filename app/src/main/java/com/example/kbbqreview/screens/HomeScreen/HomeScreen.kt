package com.example.kbbqreview.screens.HomeScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kbbqreview.*
import com.example.kbbqreview.data.firestore.Post
import com.example.kbbqreview.screens.profile.ProfileScreenState

@Composable
fun HomeScreen(
    navController: NavHostController,
) {
    val viewModel = viewModel<HomeScreenViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.getPosts()
    }

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
        val innerPadding = innerPadding
        when (state) {
            is HomeScreenState.Loaded -> {
                val loaded = state as HomeScreenState.Loaded
                HomeScreenContents(
                    posts = loaded.posts
                )
            }
            HomeScreenState.Loading -> {
                println("Current state is ${state}")
                Surface(Modifier.fillMaxSize()) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.scale(1.2f))
                    }
                }
            }
        }


    }
}


@Composable
fun HomeScreenContents(posts: List<Post>) {
    LazyColumn(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        items(posts) { post ->
            val photoList by remember {
                mutableStateOf(post.photoList)
            }

            HomeScreenItem(post, photoList)
        }

    }
}



