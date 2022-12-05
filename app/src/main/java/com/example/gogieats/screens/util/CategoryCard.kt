package com.example.gogieats.screens.util

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gogieats.R
import com.example.gogieats.ui.theme.Orange
import com.example.gogieats.ui.theme.Shadows
import com.example.gogieats.ui.theme.Yellow
import com.example.gogieats.ui.theme.spacing


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCard(
    value: MutableState<Int>,
    title: Int,
//    focusManager: FocusManager,
    icon: Int,
    description: Int,
    modifier: Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .shadow(Shadows().small, RoundedCornerShape(10.dp),
                spotColor = Color.Gray,
                ambientColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize()
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.medium
                )
        ) {
            Row(Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                Icon(
                    modifier = Modifier.weight(1f),
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Yellow
                )
                Text(
                    text = stringResource(id = title),
                    style = MaterialTheme.typography.h6,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .weight(4f)
                )
                DropdownMenu(expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.padding(20.dp)) {
                    DropdownMenuItem(onClick = {
                        expanded = false
                    }) {
                        Text(stringResource(id = description))
                    }
                }
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(id = R.drawable.icon_info),
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }


            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {

                    var sliderPosition by remember { mutableStateOf(value.value.toFloat()) }
                    val interactionSource = remember { MutableInteractionSource() }
                    Slider(
                        value = sliderPosition,
                        onValueChange = {
//                    focusManager.clearFocus()
                            sliderPosition = it
                            println("Slider is doing something: Current value: ${it}")
                        },
                        valueRange = 1f..3f,
                        onValueChangeFinished = {
                            value.value = sliderPosition.toInt()
                            // launch some business logic update with the state you hold
                            // viewModel.updateSelectedSliderValue(sliderPosition)
                        },
                        steps = 1,
                        interactionSource = interactionSource,
                        thumb = {
                            Box(modifier = Modifier.offset(3.dp, (-4).dp),
                                contentAlignment = Alignment.Center) {
                                SliderDefaults.Thumb(
                                    interactionSource = interactionSource,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Orange,
                                        disabledThumbColor = Orange,
                                    ),
                                    thumbSize = DpSize(12.dp, 12.dp))
                            }
                        },
                        track = {

                            SliderDefaults.Track(sliderPositions = it,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Orange,
                                    activeTickColor = Orange,
                                    inactiveTickColor = Color.Transparent,
//                                    inactiveTrackColor = Color.LightGray,
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(Modifier
                                    .padding(horizontal = 0.dp)
                                    .fillMaxWidth()
                                    .offset(y = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    val numbers = listOf("1", "2", "3")
                                    numbers.forEach {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.subtitle2
                                        )
                                    }
                                }
                            }

                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Orange,
                            activeTrackColor = Orange,
                            inactiveTrackColor = Color.LightGray,
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        ),
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
//                    .border(2.dp, Color.Black, shape = RoundedCornerShape(25.dp))
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth()
                ) {

                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun CardPreview() {
    val mutableState = remember {
        mutableStateOf(2)
    }
    val focusManager =
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                CategoryCard(
                    mutableState,
                    title = R.string.title_meat,
                    icon = R.drawable.icon_meat,
                    R.string.description_meat,
                    Modifier.padding(vertical = 8.dp),
                )
            }
        }
}