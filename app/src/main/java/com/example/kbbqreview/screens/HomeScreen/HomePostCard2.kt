package com.example.kbbqreview.screens.HomeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Report
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kbbqreview.HomeScreenViewModel
import com.example.kbbqreview.R
import com.example.kbbqreview.data.Category
import com.example.kbbqreview.data.firestore.Post
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.util.DisplayValuesCard
import com.example.kbbqreview.ui.theme.Yellow
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomePostCard2(
    state: PagerState,
    post: Post,
    photoList: List<Photo>,
) {

    Surface(Modifier.background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TopBox(post)
            }
//            PhotoHolder(state, photoList)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val category = listOf(
                    Category(R.drawable.icon_meat, post.valueMeat),
                    Category(R.drawable.icon_side_dishes, post.valueSideDishes),
                    Category(R.drawable.icon_amenities, post.valueAmenities),
                    Category(R.drawable.icon_atmosphere, post.valueAtmosphere),
                )
                DisplayValuesCard(category = category)
            }
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                ReviewComment(
                    post = post
                )
                AddressBar(post)
            }
        }
    }
}

@Composable
fun TopBox(post: Post, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }
    val viewModel = HomeScreenViewModel()

    if (openDialog.value) {
        AlertDialog(onDismissRequest = { openDialog.value = false },
            title = {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.report), color = Color.Red)
                    Icon(Icons.Rounded.Report, contentDescription = stringResource(R.string.report), tint = Color.Red)
                }

            },
            text = {
                Text("Please send an email to report this post.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                        val intent = viewModel.sendMail(
                            to = "hunter.krez@gmail.com",
                            subject = "Post Reported: User ID ${post.authorDisplayName}"
                        )
                        context.startActivity(intent)
                    }) {
                    Text(stringResource(R.string.go_to_email))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                    }) {
                    Text(stringResource(R.string.dismiss))
                }
            })

    }

    var expanded by remember { mutableStateOf(false) }
    val totalValue =
        post.valueAmenities + post.valueMeat + post.valueAtmosphere + post.valueSideDishes

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .background(Color.Black.copy(0.8f))
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .scale(1f)
                            .padding(end = 4.dp),
                        painter = painterResource(id = R.drawable.icon_star),
                        contentDescription = null,
                        tint = Yellow
                    )
                    Text(
                        text = totalValue.toString(),
                        style = MaterialTheme.typography.subtitle1,
                        fontSize = 22.sp,
                        color = Yellow
                    )
                }

            }
            Text(
                text = post.restaurantName,
                style = MaterialTheme.typography.subtitle1,
                fontSize = 22.sp,
                color = Color.White
            )
            IconButton(onClick = {
                expanded = true
            }) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = stringResource(R.string.more),
                    tint = Color.White
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = {
                        openDialog.value = true
                        expanded = false
                    }) {
                        Text(stringResource(R.string.report))
                    }
                }
            }
        }
    }
}
