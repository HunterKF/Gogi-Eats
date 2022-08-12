package com.example.kbbqreview.screens.story

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.example.kbbqreview.R
import com.example.kbbqreview.data.roomplaces.StoredPlace
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState


@OptIn(ExperimentalPagerApi::class)
@Composable
fun StoryItem() {
    var photos = listOf(
        R.drawable.meat,
        R.drawable.restaurant,
        R.drawable.side_dishes
    )
    var review = StoredPlace(
        itemId = 0L,
        firebaseId = "",
        name = "Show Me the Gogi",
        latitude = 35.169,
        longitude = 129.130,
        meatQuality = 3,
        banchanQuality = 2,
        amenitiesQuality = 2,
        atmosphereQuality = 2
    )
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {

        val state = rememberPagerState()
        Column {
            SliderView(state, review, photos)
            Spacer(modifier = Modifier.padding(4.dp))
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SliderView(state: PagerState, review: StoredPlace, photos: List<Int>) {


    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TopRow(review)
        }
        val imageUrl = remember {
            mutableStateOf(0)
        }
        HorizontalPager(
            state = state,
            count = photos.size, modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) { page ->
            imageUrl.value = photos[page]

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val painter =
                        rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current).data(data = imageUrl.value)
                                .apply(block = fun ImageRequest.Builder.() {
                                    placeholder(R.drawable.ic_circle)
                                    scale(Scale.FILL)
                                }).build()
                        )
                    Image(
                        painter = painter, contentDescription = "", Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxSize(), contentScale = ContentScale.Crop
                    )
                }

            }
        }

        DotsIndicator(
            totalDots = photos.size,
            selectedIndex = state.currentPage
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PointIcon(R.drawable.meat_icon, review.meatQuality)
            PointIcon(R.drawable.side_dishes_icon, review.banchanQuality)
            PointIcon(R.drawable.amenities_icon, review.amenitiesQuality)
            PointIcon(R.drawable.atmosphere_icon, review.atmosphereQuality)
        }
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            ReviewComment(
                review = review,
                text = stringResource(R.string.lorem_impsum)
            )
        }
    }
}

@Composable
fun TopRow(review: StoredPlace) {
    // Map point based on address

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
                Text("${review.amenitiesQuality}")
            }

        }
        Text(
            text = review.name,
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
fun ReviewComment(review: StoredPlace, text: String) {


    val MINIMIZED_MAX_LINES = 3
    var isExpanded by remember { mutableStateOf(false) }
    val textLayoutResultState = remember { mutableStateOf<TextLayoutResult?>(null) }
    var isClickable by remember { mutableStateOf(false) }
    var finalText by remember { mutableStateOf(text) }
    val textLayoutResult = textLayoutResultState.value

    LaunchedEffect(textLayoutResult) {
        if (textLayoutResult == null) return@LaunchedEffect

        when {
            isExpanded -> {
                finalText = "$text Show Less"

            }
            !isExpanded && textLayoutResult.hasVisualOverflow -> {
                val lastCharIndex = textLayoutResult.getLineEnd(MINIMIZED_MAX_LINES - 2)
                val showMoreString = "... Show More"
                val adjustedText = text
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
                append(review.name)
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
    AddressBar(review)
}

@Composable
fun AddressBar(review: StoredPlace) {
    val mapIntent: Intent = Uri.parse(
        "geo:0,0?q=1600+Amphitheatre+Parkway,+Mountain+View,+California"
    ).let { location ->
        // Or map point based on latitude/longitude
        // val location: Uri = Uri.parse("geo:37.422219,-122.08364?z=14") // z param is zoom level
        Intent(Intent.ACTION_VIEW, location)
    }
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.weight(6f),
            text = "4 Haeun-daero 594 beonga-gil, Busan",
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
                        .background(color = Color.DarkGray)
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