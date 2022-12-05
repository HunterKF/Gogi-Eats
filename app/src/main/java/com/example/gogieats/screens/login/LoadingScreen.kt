package com.example.gogieats.screens.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import com.example.gogieats.screens.util.CustomCircularProgress

@Composable
fun LoadingScreen() {
    Surface(Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center) {
            CustomCircularProgress(modifier = Modifier.scale(1.2f))
        }
    }
}