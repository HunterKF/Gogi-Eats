package com.example.gogieats.screens.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gogieats.R
import com.example.gogieats.data.Category
import com.example.gogieats.ui.theme.Orange
import com.example.gogieats.ui.theme.Shadows

@Composable
fun DisplayValuesCard(
    modifier: Modifier = Modifier,
    category: List<Category>,
) {
    Card(
        modifier = modifier
            .shadow(Shadows().small,
                Shapes().medium,
                spotColor = Color.Gray,
                ambientColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            category.forEach {
                CategoryDisplay(icon = it.icon, value = it.value)
            }
        }
    }
}

@Composable
fun CategoryDisplay(icon: Int, value: Int) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.height(50.dp)
    ) {

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = Orange,
                modifier = Modifier.align(Alignment.Center).scale(0.8f)
            )
        }
        Text(
            value.toString()
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewCategory() {
    val category = listOf(
        Category(R.drawable.icon_meat, 2),
        Category(R.drawable.icon_side_dishes, 2),
        Category(R.drawable.icon_amenities, 2),
        Category(R.drawable.icon_atmosphere, 2)
    )
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        DisplayValuesCard(category = category, modifier = Modifier.fillMaxWidth(0.8f))
    }
}
