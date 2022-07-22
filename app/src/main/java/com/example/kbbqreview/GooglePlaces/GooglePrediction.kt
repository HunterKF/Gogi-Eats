package com.example.kbbqreview.GooglePlaces

data class GooglePrediction(
    val description: String,
    val terms: List<GooglePredictionTerm>
)
