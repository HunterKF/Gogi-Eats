package com.example.kbbqreview.screens.util

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kbbqreview.ui.theme.Orange


@Composable
fun OrangeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = { onClick() },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Orange
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(8.dp)
        )
    }
}