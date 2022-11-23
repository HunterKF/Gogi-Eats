package com.example.kbbqreview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val vector: ImageVector, val icon: Int?) {
    object MapScreen : Screen("map_screen", "Map", Icons.Rounded.Map, icon = R.drawable.icon_map)
    object MainCamera : Screen("main_camera", "MultiCamera", Icons.Rounded.Camera, icon = null )
    object ProfileCamera : Screen("profile_camera", "SingleCamera", Icons.Rounded.Camera, icon = null)
    object FromAddChooseLocation : Screen("from_add_choose_location", "Choose Location", Icons.Rounded.LocalActivity, icon = null)
    object FromEditChooseLocation : Screen("from_edit_choose_location", "Choose Location", Icons.Rounded.LocalActivity, icon = null)
    object Profile : Screen("profile", "profile", Icons.Rounded.Person, icon = R.drawable.icon_profile)
    object HomeScreen : Screen("screen", "Story", Icons.Rounded.Home, icon = R.drawable.icon_home)
    object AddReview : Screen("add_review", "Review", Icons.Rounded.Add, icon = R.drawable.icon_review)
    object Login : Screen("sign_in", "Sign in", Icons.Rounded.Login, icon = null)
    object EditScreen : Screen("edit_screen", "Edit", Icons.Rounded.Edit, icon = null)
    object PluckGallery : Screen("pluck_gallery", "Custom Gallery", Icons.Rounded.PhotoLibrary, icon = null)
    object CapturedPhotoView : Screen("captured_photo_view", "Photo", Icons.Rounded.PhotoLibrary, icon = null)


}
val items = listOf(
    Screen.HomeScreen,
    Screen.MapScreen,
    Screen.AddReview,
    Screen.Profile
)
