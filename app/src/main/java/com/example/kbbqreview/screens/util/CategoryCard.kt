package com.example.kbbqreview.screens.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kbbqreview.R
import com.example.kbbqreview.ui.theme.Orange
import com.example.kbbqreview.ui.theme.Shadows
import com.example.kbbqreview.ui.theme.Yellow
import com.example.kbbqreview.ui.theme.spacing

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
            .shadow(Shadows().small, RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier
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
                    modifier = Modifier.scale(0.6f),
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Yellow
                )
                Text(
                    text = stringResource(id = title),
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .weight(1f)
                )
                DropdownMenu(expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()) {
                    DropdownMenuItem(onClick = {
                        expanded = false
                    }) {
                        Text(stringResource(id = description))
                    }
                }
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Rounded.Info,
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
//                    .border(2.dp, Color.Black, shape = RoundedCornerShape(25.dp))

                        .fillMaxWidth()
                ) {

                    var sliderPosition by remember { mutableStateOf(value.value.toFloat()) }
                    Slider(
                        value = sliderPosition,
                        onValueChange = {
//                    focusManager.clearFocus()
                            sliderPosition = it
                        },
                        valueRange = 1f..3f,
                        onValueChangeFinished = {
                            value.value = sliderPosition.toInt()
                            // launch some business logic update with the state you hold
                            // viewModel.updateSelectedSliderValue(sliderPosition)
                        },
                        steps = 1,
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
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                ) {
                    val numbers = listOf("1", "2", "3")
                    numbers.forEach {
                        Text(
                            it
                        )
                    }
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
                    icon = R.drawable.meat_icon,
                    R.string.description_meat,
                    Modifier.padding(vertical = 8.dp),
                )
            }
        }
}