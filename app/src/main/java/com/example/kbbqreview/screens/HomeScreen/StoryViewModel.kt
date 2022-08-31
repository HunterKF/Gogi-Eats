package com.example.kbbqreview.screens.HomeScreen

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.kbbqreview.R

class StoryViewModel(): ViewModel() {
    var imageList = mutableStateListOf<Int>(
        R.drawable.meat,
        R.drawable.restaurant,
        R.drawable.side_dishes
    )

    fun addImage() {
        imageList.add(R.drawable.meat)
        imageList.add(R.drawable.restaurant)
        imageList.add(R.drawable.side_dishes)
    }
}