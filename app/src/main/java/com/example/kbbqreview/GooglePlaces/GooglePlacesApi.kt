package com.example.kbbqreview.GooglePlaces

import retrofit2.http.GET
import retrofit2.http.Query


interface GooglePlacesApi {
    @GET("maps/api/place/autocomplete/json")
    suspend fun getPredictions(
        @Query("key") key: String ="AIzaSyBMv8xNYA8H-MKYs9GfF0dFL0TAxSV-Jms",
        @Query("types") types: String = "restaurant",
        @Query("input") input: String
    ): GooglePredictionsResponse

    companion object{
        const val BASE_URL = "https://maps.googleapis.com/"
    }
}

