package com.example.kbbqreview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val vector: ImageVector) {
    object MapScreen : Screen("map_screen", "Map", Icons.Sharp.Map)
    object MainContentCamera : Screen("main_content_camera", "Camera", Icons.Sharp.Camera)
    object ChooseLocationMap : Screen("choose_location_map", "Choose Location", Icons.Sharp.LocalActivity)
    object Profile : Screen("profile", "Profile", Icons.Sharp.Person)
    object Story : Screen("screen", "Story", Icons.Sharp.Home)
    object AddReview : Screen("add_review", "Review", Icons.Sharp.Add)
    object CapturedPhotoView : Screen("captured_photo_view", "Photo", Icons.Sharp.PhotoLibrary)


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
