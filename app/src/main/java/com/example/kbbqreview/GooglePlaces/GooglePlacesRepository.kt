package com.example.kbbqreview.GooglePlaces

import android.util.Log
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class GooglePlacesRepository @Inject constructor(
    private val api: GooglePlacesApi,
){
    suspend fun getPredictions(input: String): Resource<GooglePredictionsResponse> {
        val response = try {
            api.getPredictions(input = input)
        } catch (e: Exception) {
            Log.d("Rently", "Exception: ${e}")
            return Resource.Error("Failed prediction")
        }

        return Resource.Success(response)
    }
}