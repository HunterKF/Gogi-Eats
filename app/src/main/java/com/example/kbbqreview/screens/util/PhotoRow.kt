package com.example.kbbqreview.screens.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kbbqreview.R
import com.example.kbbqreview.Screen
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.AddNewPhoto
import com.example.kbbqreview.screens.HomeScreen.PhotoCard
import com.example.kbbqreview.screens.HomeScreen.Scrim
import com.example.kbbqreview.ui.theme.Orange
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
        mainAxisAlignment = FlowMainAxisAlignment.Start,
        mainAxisSpacing = 4.dp,
        crossAxisSpacing = 4.dp
    ) {
        val itemSize: Dp = (LocalConfiguration.current.screenWidthDp.dp / 3)
        PhotoCard2(
            allPhotos,
            modifier = Modifier
                .size(itemSize - 22.dp)
                .clip(RoundedCornerShape(5.dp))
                .aspectRatio(1f)
        )
        AddNewPhoto2(
            modifier = Modifier
                .shadow(5.dp, RoundedCornerShape(10.dp),
                    spotColor = Color.Gray,
                    ambientColor = Color.Transparent)
                .size(itemSize - 22.dp)
                .clip(RoundedCornerShape(10.dp))
                .aspectRatio(1f),
            navController = navController
        )
    }

}

@Composable
fun AddNewPhoto2(modifier: Modifier, navController: NavHostController) {
    Box(
        modifier = modifier.background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = { navController.navigate(Screen.MainCamera.route) }) {
            Icon(painter = painterResource(id = R.drawable.icon_plus),
                null,
                tint = Orange,
                modifier = Modifier.scale(1.7f))
        }

    }
}

@Composable
fun PhotoCard2(allPhotos: SnapshotStateList<Photo>, modifier: Modifier) {
    fun removePhoto(photo: Photo) {
        allPhotos.remove(photo)
    }
    allPhotos.forEach { photo ->
        Box(
            modifier = modifier
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(
                        data = photo.localUri
                    )
                    .placeholder(R.drawable.ic_image_placeholder)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Scrim(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(Alignment.BottomCenter)
            )
            IconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                onClick = { removePhoto(photo) }) {
                Icon(Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.remove_photo),
                    tint = Color.White)
            }
        }
    }

}