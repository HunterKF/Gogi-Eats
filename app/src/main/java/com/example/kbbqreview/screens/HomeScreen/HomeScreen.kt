package com.example.kbbqreview.screens.HomeScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kbbqreview.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun HomeScreen(
    navController: NavHostController,
    applicationViewModel: ApplicationViewModel,
    navigationToSignIn: () -> Unit
) {

    val viewModel = viewModel<HomeScreenViewModel>()
    val state by viewModel.state.collectAsState()
    val storyFeed by applicationViewModel.storyFeed.observeAsState()
    val isRefreshing by applicationViewModel.isRefreshing.collectAsState()
    /*LaunchedEffect(key1 = isRefreshing) {
        applicationViewModel.getReviews()
    }*/

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

            HomeScreenState.Loading -> LoadingScreen()
            HomeScreenState.SignInRequired -> LaunchedEffect(key1 = Unit) {
                navigationToSignIn()
            }
        }


    }

}

@Composable
fun HomeScreenContents(posts: List<Post>) {
    /*val isRefreshing by applicationViewModel.isRefreshing.collectAsState()
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { applicationViewModel.refresh() }) {*/
    LazyColumn(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        items(posts) { post ->
            HomeScreenItem(post)
        }

    }
}

@Composable
fun LoadingScreen() {
    Text("Hello")
}

