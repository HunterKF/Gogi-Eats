package com.example.gogieats.screens.HomeScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.gogieats.*
import com.example.gogieats.R
import com.example.gogieats.data.firestore.Post
import com.example.gogieats.data.user.User
import com.example.gogieats.screens.util.CustomCircularProgress
import com.example.gogieats.ui.theme.Orange
import com.example.gogieats.util.BlockUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun HomeScreen(
    navController: NavHostController,
) {
    val viewModel = viewModel<HomeScreenViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.getPosts()
    }
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
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            null,

                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .size(62.dp)
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
                        CustomCircularProgress(/*Modifier.scale(1.2f)*/)
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
        contentPadding = PaddingValues(bottom = 100.dp),
        modifier = Modifier.background(Color.White)
    ) {
        val currentUser = Firebase.auth.currentUser
        var blockedAccounts = arrayListOf<User>()

        currentUser?.let {
             blockedAccounts = BlockUser.getBlockedAccounts(currentUser.uid)
        }

        items(posts) { post ->
            val containsPost = blockedAccounts.filter { it.uid == post.userId }
            if (containsPost.isNotEmpty()) {
                Box(
                    modifier = Modifier.aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "This account was blocked.",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "To unblock, please go to account settings.",
                            textAlign = TextAlign.Center
                        )
                    }

                }

            } else {
                val photoList by remember {
                    mutableStateOf(post.photoList)
                }

                HomeScreenItem(post, photoList)
            }
        }

    }
}



