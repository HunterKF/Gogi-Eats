package com.example.kbbqreview.data.storyfeed

import com.example.kbbqreview.data.photos.Photo
import com.example.kbbqreview.data.roomplaces.StoredPlace

data class StoryItem(
    val reviews: StoredPlace,
    val photos: List<Photo>

)
