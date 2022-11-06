package com.example.kbbqreview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val vector: ImageVector) {
    object MapScreen : Screen("map_screen", "Map", Icons.Rounded.Map)
    object MainCamera : Screen("main_camera", "MultiCamera", Icons.Rounded.Camera)
    object ProfileCamera : Screen("profile_camera", "SingleCamera", Icons.Rounded.Camera)
    object FromAddChooseLocation : Screen("from_add_choose_location", "Choose Location", Icons.Rounded.LocalActivity)
    object FromEditChooseLocation : Screen("from_edit_choose_location", "Choose Location", Icons.Rounded.LocalActivity)
    object Profile : Screen("profile", "profile", Icons.Rounded.Person)
    object HomeScreen : Screen("screen", "Story", Icons.Rounded.Home)
    object AddReview : Screen("add_review", "Review", Icons.Rounded.Add)
    object Login : Screen("sign_in", "Sign in", Icons.Rounded.Login)
    object EditScreen : Screen("edit_screen", "Edit", Icons.Rounded.Edit)
    object PluckGallery : Screen("pluck_gallery", "Custom Gallery", Icons.Rounded.PhotoLibrary)
    object CapturedPhotoView : Screen("captured_photo_view", "Photo", Icons.Rounded.PhotoLibrary)


}
val items = listOf(
    Screen.MapScreen,
    Screen.HomeScreen,
    Screen.AddReview,
    Screen.Profile
)
