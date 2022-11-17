package com.example.kbbqreview.screens.util

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.kbbqreview.Screen
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.AddNewPhoto
import com.example.kbbqreview.screens.HomeScreen.PhotoCard
import com.example.kbbqreview.ui.theme.Shadows
import com.example.kbbqreview.ui.theme.Shapes
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode

@Composable
fun PhotoRow(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    allPhotos: SnapshotStateList<Photo>,
) {
    FlowRow(
        modifier = modifier,
        mainAxisSize = SizeMode.Expand,
        mainAxisAlignment = FlowMainAxisAlignment.Start
    ) {
        val itemSize: Dp = (LocalConfiguration.current.screenWidthDp.dp / 3)
        PhotoCard(
            allPhotos,
            modifier = Modifier
                .size(itemSize - 20.dp)
                .clip(RoundedCornerShape(5.dp))
                .aspectRatio(1f)
        )
        AddNewPhoto2(
            modifier = Modifier
                .shadow(0.dp, RoundedCornerShape(10.dp))
            .size (itemSize - 20.dp)
            .clip(RoundedCornerShape(10.dp))
            .aspectRatio(1f),
        navController = navController
        )
    }

}

@Composable
fun AddNewPhoto2(modifier: Modifier, navController: NavHostController) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = { navController.navigate(Screen.MainCamera.route) }) {
            Icon(Icons.Rounded.Add, null)
        }

    }
}