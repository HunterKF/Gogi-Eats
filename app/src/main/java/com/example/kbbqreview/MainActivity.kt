package com.example.kbbqreview

import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.kbbqreview.ui.theme.KBBQReviewTheme
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

class MainActivity : ComponentActivity() {

//    val locationViewModel = ApplicationViewModel(application = this.application)
    private val applicationViewModel: ApplicationViewModel by viewModels<ApplicationViewModel>()

    private lateinit var mPlacesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val location by applicationViewModel.getLocationLiveData().observeAsState()
            KBBQReviewTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    location?.let { MapScreen(it) }
                }
            }
            prepLocationUpdates()
        }
    }

    private fun prepLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            requestLocationUpdates()
        } else {
            requestSinglePermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestSinglePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        isGranted ->
        if (isGranted) {
            requestLocationUpdates()
        } else {
            Toast.makeText(this, "GPS Unavailable", Toast.LENGTH_LONG).show()
        }
    }
    private fun requestLocationUpdates() {
        applicationViewModel.startLocationUpdates()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        Places.initialize(this, "AIzaSyBMv8xNYA8H-MKYs9GfF0dFL0TAxSV-Jms")
        mPlacesClient = Places.createClient(this)
    }

}
