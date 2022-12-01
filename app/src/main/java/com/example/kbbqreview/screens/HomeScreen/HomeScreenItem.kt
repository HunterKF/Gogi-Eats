package com.example.kbbqreview.screens.HomeScreen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Report
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kbbqreview.HomeScreenViewModel
import com.example.kbbqreview.data.firestore.Post
import com.example.kbbqreview.R
import com.example.kbbqreview.data.Category
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.data.user.FullUser
import com.example.kbbqreview.screens.profile.ProfileViewModel
import com.example.kbbqreview.screens.util.DisplayValuesCard
import com.example.kbbqreview.screens.util.ShadowDivider
import com.example.kbbqreview.ui.theme.Brown
import com.example.kbbqreview.ui.theme.Orange
import com.example.kbbqreview.ui.theme.UserReviewInfo
import com.example.kbbqreview.util.AddressMap
import com.example.kbbqreview.util.ShareUtils
import com.example.kbbqreview.util.UserViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState


@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomeScreenItem(
    storyItem: Post,
    photoList: List<Photo>,
) {

    val userViewModel = UserViewModel()
    LaunchedEffect(key1 = Unit, block = {
        userViewModel.getUser(storyItem.userId)
    })


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        val state = rememberPagerState()
        Column {
            HomePostCard(state = state,
                post = storyItem,
                photoList = photoList,
                modifier = Modifier.padding(bottom = 12.dp),
                postUser =  userViewModel.user
            )
        }
    }


}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomePostCard(
    state: PagerState,
    post: Post,
    photoList: List<Photo>,
    modifier: Modifier = Modifier,
    postUser: MutableState<FullUser>,
    profileViewModel: ProfileViewModel? = null
) {

    Surface(Modifier.background(Color.White)) {
        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                TopBox(post,
                    modifier = Modifier
                        .zIndex(1f)
                        .align(Alignment.TopCenter),
                profileViewModel = profileViewModel)
                PhotoHolder(state, photoList, modifier = Modifier)
                val category = listOf(
                    Category(R.drawable.icon_meat, post.valueMeat),
                    Category(R.drawable.icon_side_dishes, post.valueSideDishes),
                    Category(R.drawable.icon_amenities, post.valueAmenities),
                    Category(R.drawable.icon_atmosphere, post.valueAtmosphere),
                )
                Column(Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.8f),
                    verticalArrangement = Arrangement.Center) {
                    DotsIndicator(
                        totalDots = photoList.size,
                        selectedIndex = state.currentPage,
                        modifier = Modifier.offset(y = (8).dp)
                    )
                    DisplayValuesCard(category = category, modifier = Modifier.offset(y = 28.dp))
                }
            }
            Spacer(Modifier.height(32.dp))
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(8.dp))
                UserReviewInfo(post = post,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    postUser = postUser)
                Spacer(Modifier.height(8.dp))
                ShadowDivider(modifier = Modifier.fillMaxWidth(0.8f))
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}


@OptIn(ExperimentalPagerApi::class)
@Composable
private fun PhotoHolder(
    state: PagerState,
    photoList: List<Photo>,
    modifier: Modifier = Modifier,
) {

    val emptyPhoto = Photo(
        "",
        "",
        "",
        0
    )
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        HorizontalPager(
            state = state,
            count = photoList.size, modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.1f)
                .clip(RoundedCornerShape(bottomEnd = 50.dp, bottomStart = 50.dp))
        ) { page ->
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(data = if (photoList.isNotEmpty()) photoList[page].remoteUri else emptyPhoto)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .crossfade(true)
                            .build(), contentDescription = null, Modifier
                            .padding(vertical = 0.dp)
                            .fillMaxSize(), contentScale = ContentScale.Crop
                    )
                }

            }
        }
        Box(
            modifier = Modifier
                .zIndex(2f)
                .align(Alignment.BottomCenter)
                .offset(y = (-20).dp),
        ) {

        }
        Box(
            Modifier
                .zIndex(1f)
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Scrim(
                modifier = Modifier

                    .fillMaxWidth()
                    .height(56.dp)
            )
        }
    }
}

@Composable
fun TopRow(post: Post) {
    // Map point based on address
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
                    Icon(Icons.Rounded.Report,
                        contentDescription = stringResource(R.string.report),
                        tint = Color.Red)
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 8.dp)
                .border(1.dp, Color.Black, RoundedCornerShape(15.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.scale(0.75f),
                    painter = painterResource(id = R.drawable.ic_baseline_star_rate_24),
                    contentDescription = null
                )
                Text(totalValue.toString())
            }

        }
        Text(
            text = post.restaurantName,
            style = MaterialTheme.typography.h6
        )
        IconButton(onClick = {
            expanded = true
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_more),
                contentDescription = stringResource(R.string.more)
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

@Composable
fun ReviewComment(post: Post) {
    val MINIMIZED_MAX_LINES = 3
    var isExpanded by remember { mutableStateOf(false) }
    val textLayoutResultState = remember { mutableStateOf<TextLayoutResult?>(null) }
    var isClickable by remember { mutableStateOf(false) }
    var finalText by remember { mutableStateOf(post.authorText) }
    val textLayoutResult = textLayoutResultState.value

    LaunchedEffect(textLayoutResult) {
        if (textLayoutResult == null) return@LaunchedEffect

        when {
            isExpanded -> {
                finalText = "${post.authorText} Show Less"

            }
            !isExpanded && textLayoutResult.hasVisualOverflow -> {
                val lastCharIndex = textLayoutResult.getLineEnd(MINIMIZED_MAX_LINES - 2)
                val showMoreString = "... Show More"
                val adjustedText = post.authorText
                    .substring(startIndex = 0, endIndex = lastCharIndex)
                    .dropLast(showMoreString.length)
                    .dropLastWhile { it == ' ' || it == '.' }

                finalText = "$adjustedText$showMoreString"

                isClickable = true
            }
        }
    }
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(post.authorDisplayName)
                append(" ")
            }
            append(finalText)
        },
        maxLines = if (isExpanded) Int.MAX_VALUE else MINIMIZED_MAX_LINES,
        onTextLayout = { textLayoutResultState.value = it },
        modifier = Modifier
            .clickable(enabled = isClickable) { isExpanded = !isExpanded }
            .animateContentSize(),
    )

}

@Composable
fun AddressBar(post: Post) {

    val context = LocalContext.current
    val emptyPhoto = Photo(
        "",
        "",
        "",
        0
    )
    val address = AddressMap.getAddressFromLocation(context,
        post.location!!.latitude,
        post.location.longitude)
    val shareIntent = ShareUtils.genericShare(
        context = context,
        text = post.restaurantName,
        address = address,
        photoUri = if (post.photoList.isEmpty()) emptyPhoto.remoteUri else post.photoList.first().remoteUri
    )
    val mapIntent: Intent = Uri.parse(
        "geo:${post.location.latitude},${post.location.longitude}?z=8"
    ).let { location ->
        // Or map point based on latitude/longitude
        // val location: Uri = Uri.parse("geo:37.422219,-122.08364?z=14") // z param is zoom level
        Intent(Intent.ACTION_VIEW, location)
    }

    val type = "text/plain"
    val subject = post.restaurantName
    val extraText = AddressMap.getAddressFromLocation(
        context,
        post.location!!.latitude,
        post.location.longitude
    )

    val intent = Intent(Intent.ACTION_SEND)
    intent.type = type
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, extraText)
    if (post.photoList.isNotEmpty()) {
        intent.putExtra(Intent.EXTRA_STREAM, post.photoList.first().remoteUri)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.weight(6f),
            text = AddressMap.getAddressFromLocation(
                context,
                post.location!!.latitude,
                post.location.longitude
            ),
            fontSize = 15.sp,
            color = Brown
        )
        IconButton(
            modifier = Modifier.weight(1f),
            onClick = {
                context.startActivity(shareIntent)
            }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_send_24),
                contentDescription = stringResource(R.string.share)
            )
        }
        IconButton(modifier = Modifier.weight(1f), onClick = {
            context.startActivity(mapIntent)
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_map_marker_24),
                contentDescription = stringResource(R.string.open_map)
            )
        }
    }
}

fun checkPhoto(photoList: List<Photo>, emptyPhoto: Photo): String? {
    var uri = ""
    if (photoList.isNotEmpty()) {
        uri = photoList[0].remoteUri
    } else {
        uri = emptyPhoto.remoteUri
    }
    return uri
}


@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(), horizontalArrangement = Arrangement.Center
    ) {

        items(totalDots) { index ->
            if (index == selectedIndex) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color = Orange)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color = Color.White)
                )
            }

            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            }
        }
    }
}

@Composable
fun PointIcon(icon: Int, value: Int) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            modifier = Modifier.scale(0.5f),
            painter = painterResource(id = icon),
            contentDescription = null
        )

        Text("$value")
    }
}
