package com.example.gogieats.screens.util

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gogieats.ui.theme.Orange


@Composable
fun OrangeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = { onClick() },
        modifier = modifier.clip(RoundedCornerShape(10.dp)),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Orange
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.h6,
            fontSize = 22.sp,
            color = Color.White,
            modifier = Modifier.padding(8.dp)
        )
    }
}