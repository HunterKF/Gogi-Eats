package com.example.kbbqreview.screens.story

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.kbbqreview.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState


@OptIn(ExperimentalPagerApi::class)
@Composable
fun StoryItem(storyViewModel: StoryViewModel) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        /*LaunchedEffect(key1 = storyViewModel.imageList) {
            storyViewModel.addImage()
        }*/

        val state = rememberPagerState()
        Column {
            SliderView(state, storyViewModel)
            Spacer(modifier = Modifier.padding(4.dp))
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SliderView(state: PagerState, storyViewModel: StoryViewModel) {


    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "UserName",
                style = MaterialTheme.typography.h6
            )
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_more),
                    contentDescription = "More"
                )
            }
        }


        val imageUrl =
            remember { mutableStateOf(0) }
        HorizontalPager(
            state = state,
            count = storyViewModel.imageList.size, modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) { page ->
            imageUrl.value = storyViewModel.imageList[page]

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomCenter) {

                    val painter =
                        rememberImagePainter(data = storyViewModel.imageList[page], builder = {
                            placeholder(R.drawable.ic_circle)
                            scale(coil.size.Scale.FILL)
                        })
                    Image(
                        painter = painter, contentDescription = "", Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .fillMaxSize(), contentScale = ContentScale.Crop
                    )
                }

            }
        }

        DotsIndicator(
            totalDots = storyViewModel.imageList.size,
            selectedIndex = state.currentPage
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("To be made...")
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