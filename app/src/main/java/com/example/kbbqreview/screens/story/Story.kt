package com.example.kbbqreview.screens.story

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberImagePainter
import coil.size.Scale
import com.example.kbbqreview.ApplicationViewModel
import com.example.kbbqreview.R
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.items

@Composable
fun Story(navController: NavHostController, applicationViewModel: ApplicationViewModel) {

    val storyFeed by applicationViewModel.storyFeed.observeAsState()

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
        LazyColumn(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(innerPadding)
        ) {
            storyFeed?.let {
                items(storyFeed!!.storyList) { storyItem ->
                    StoryItem(storyItem)
                }
            }
/*
            items(userList) { user ->
                Text(user.uid)
            }
            items(photos) { photo ->
                Review(photo)
                Text(photo.id)
                Text(photo.dateTaken.toString())
            }
            items(reviews) { review ->
                Text(review.name)
                Text(review.firebaseId)
            }*/
        }
    }

}

@Composable
fun Review(photo: Photo) {
        val painter =
            rememberImagePainter(data = photo.remoteUri, builder = {
                placeholder(R.drawable.ic_circle)
                scale(Scale.FILL)
            })
        Image(
            painter = painter, contentDescription = "", Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(10.dp))
                .fillMaxSize(), contentScale = ContentScale.Crop
        )

}
