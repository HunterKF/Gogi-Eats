package com.example.kbbqreview.screens.login.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.example.kbbqreview.ui.theme.Brown
import com.example.kbbqreview.ui.theme.OffWhite

@Composable
fun DividedBackground(modifier: Modifier = Modifier) {
    Column(modifier = modifier.zIndex(0f)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Brown),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(4f)
                .background(OffWhite),
        )
    }
}