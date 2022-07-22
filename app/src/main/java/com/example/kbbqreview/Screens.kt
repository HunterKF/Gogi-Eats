package com.example.kbbqreview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val vector: ImageVector) {
    object MapScreen : Screen("map_screen", "Map", Icons.Sharp.Map)
    object CameraContainer : Screen("camera_container", "Camera", Icons.Sharp.Camera)
    object Profile : Screen("profile", "Profile", Icons.Sharp.Person)
    object Story : Screen("screen", "Story", Icons.Sharp.Home)
    object AddReview : Screen("add_review", "Review", Icons.Sharp.Add)


    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
val items = listOf(
    Screen.MapScreen,
    Screen.Story,
    Screen.AddReview
)
