package com.example.gogieats.screens.util

import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gogieats.ui.theme.Orange

@Composable
fun CustomCircularProgress(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier.size(100.dp),
        color = Orange,
    )
}