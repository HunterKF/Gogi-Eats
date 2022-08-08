package com.example.kbbqreview.data.storyfeed

data class StoryItem(
    var id: String? = "",
    var userId: String? = "",
    var latitude: Double? = 0.0,
    var longitude: Double? = 0.0,
    var meatQuality: Int? = 0,
    var banchanQuality: Int? = 0,
    var amenitiesQuality: Int? = 0,
    var atmosphereQuality: Int? = 0,
    var remoteUri: String? = "",
)
