package com.example.gogieats.screens.util

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShadowDivider(modifier: Modifier = Modifier) {
    Divider(modifier = modifier
        .fillMaxWidth()
        .shadow(6.dp, RoundedCornerShape(1.dp), spotColor = Color.Gray, ambientColor = Color.LightGray ),
        color = Color.Transparent
    )
}