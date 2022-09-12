package com.example.kbbqreview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.sharp.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val vector: ImageVector) {
    object MapScreen : Screen("map_screen", "Map", Icons.Rounded.Map)
    object MainContentCamera : Screen("main_content_camera", "Camera", Icons.Rounded.Camera)
    object ChooseLocationMap : Screen("choose_location_map", "Choose Location", Icons.Rounded.LocalActivity)
    object Profile : Screen("profile", "profile", Icons.Rounded.Person)
    object HomeScreen : Screen("screen", "Story", Icons.Rounded.Home)
    object AddReview : Screen("add_review", "Review", Icons.Rounded.Add)
    object Login : Screen("sign_in", "Sign in", Icons.Rounded.Login)
    object EditScreen : Screen("edit_screen", "Edit", Icons.Rounded.Edit)
    object CapturedPhotoView : Screen("captured_photo_view", "Photo", Icons.Rounded.PhotoLibrary)


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
    Screen.HomeScreen,
    Screen.AddReview,
    Screen.Profile
)
