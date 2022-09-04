package com.example.kbbqreview.screens.HomeScreen

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kbbqreview.Post
import com.example.kbbqreview.R
import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.screens.addreview.ReviewViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState


@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomeScreenItem(storyItem: Post) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        val state = rememberPagerState()
        Column {
            PostCard(state, storyItem)
            Spacer(modifier = Modifier.padding(4.dp))
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PostCard(state: PagerState, post: Post) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TopRow(post)
        }
        PhotoHolder(state, post)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PointIcon(R.drawable.meat_icon, post.valueMeat.toInt())
            PointIcon(R.drawable.side_dishes_icon, post.valueSideDishes.toInt())
            PointIcon(R.drawable.amenities_icon, post.valueAmenities.toInt())
            PointIcon(R.drawable.atmosphere_icon, post.valueAtmosphere.toInt())
        }
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            ReviewComment(
                post = post
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun PhotoHolder(
    state: PagerState,
    post: Post,
) {
    val photoList by remember {
        mutableStateOf(post.photoList)
    }
    val emptyPhoto = Photo(
        "",
        "",
        "",
        0
    )
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        HorizontalPager(
            state = state,
            count = photoList.size, modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
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
                            .build(), contentDescription = "", Modifier
                            .padding(vertical = 8.dp)
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
            DotsIndicator(
                totalDots = photoList.size,
                selectedIndex = state.currentPage
            )
        }
        Box(
            Modifier
                .zIndex(1f)
                .fillMaxWidth()
                .offset(y = (-8).dp)
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
            /*TODO*/
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_more),
                contentDescription = "More"
            )
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
    AddressBar(post)
}

@Composable
fun AddressBar(review: Post) {
    val reviewViewModel = ReviewViewModel()
    val context = LocalContext.current
    val mapIntent: Intent = Uri.parse("geo:${review.location!!.latitude},${review.location!!.longitude}?z=14"
    ).let { location ->
        // Or map point based on latitude/longitude
        // val location: Uri = Uri.parse("geo:37.422219,-122.08364?z=14") // z param is zoom level
        Log.d("Address", "Map has launched. Value of location: ${location}")
        println("Map has launched. Value of location: ${location}")
        Intent(Intent.ACTION_VIEW, location)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.weight(6f),
            text = reviewViewModel.getAddressFromLocation(
                context,
                review.location!!.latitude,
                review.location.longitude
            ),
            fontWeight = FontWeight.SemiBold
        )
        IconButton(modifier = Modifier.weight(1f), onClick = { /*TODO*/ }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_send_24),
                contentDescription = "Share"
            )
        }
        IconButton(modifier = Modifier.weight(1f), onClick = {
            context.startActivity(mapIntent)
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_map_marker_24),
                contentDescription = "Launch map"
            )
        }
    }
}


@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int
) {

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(), horizontalArrangement = Arrangement.Center
    ) {

        items(totalDots) { index ->
            if (index == selectedIndex) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color = Color.White)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color = Color.LightGray)
                )
            }

            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            }
        }
    }
}

@Composable
fun PointIcon(photo: Int, value: Int) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            modifier = Modifier.scale(0.5f),
            painter = painterResource(id = photo),
            contentDescription = null
        )

        Text("$value")
    }
}