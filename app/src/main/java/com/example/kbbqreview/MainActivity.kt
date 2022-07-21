package com.example.kbbqreview

import android.Manifest
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.compose.rememberNavController
import com.example.kbbqreview.camera.CameraViewModel
import com.example.kbbqreview.ui.theme.KBBQReviewTheme
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private var uri: Uri? = null
    private lateinit var currentImagePath: String
    private var strUri by mutableStateOf("")

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var photoUri: Uri
    private var shouldShowPhoto: MutableState<Boolean> = mutableStateOf(false)
    val cameraViewModel: CameraViewModel = CameraViewModel()

    //    val locationViewModel = ApplicationViewModel(application = this.application)
    private val applicationViewModel: ApplicationViewModel by viewModels<ApplicationViewModel>()

    private lateinit var mPlacesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {


            val permissionGranted by cameraViewModel.permissionGrantedCamera.observeAsState()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
//            val location by applicationViewModel.getLocationLiveData().observeAsState()
            KBBQReviewTheme {
                val navController = rememberNavController()


                Navigation(
                    navController = navController,
                    applicationViewModel = applicationViewModel
                )
            }
            requestCameraPermission()
            prepLocationUpdates()

            outputDirectory = getOutputDirectory()
            cameraExecutor = Executors.newSingleThreadExecutor()
        }
    }

    private fun handleImageCapture(uri: Uri) {
        Log.i("cameraHandle", "Image captured: $uri")
        cameraViewModel.permissionGrantedCamera.value = false

        photoUri = uri
        shouldShowPhoto.value = true
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    private fun prepLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PERMISSION_GRANTED
        ) {
            requestLocationUpdates()
        } else {
            requestSinglePermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestSinglePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
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

    private val getCameraImage =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                Log.i("Camera", "Image Location: $uri")
                strUri = uri.toString()
            } else {
                Log.e("Camera", "Image not saved. $uri")
            }
        }

    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

    private fun hasExternalStoragePermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun invokeCamera() {
        val file = createImageFile()
        try {
            uri = FileProvider.getUriForFile(this, "com.example.kbbqreview.fileprovider", file)
        } catch (e: Exception) {
            Log.e("invokeCamera", "Error: ${e.message}")
            var foo = e.message
        }
        getCameraImage.launch(uri)
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "Review_${timestamp}",
            ".jpg",
            imageDirectory
        ).apply {
            currentImagePath = absolutePath
        }
    }

    private val requestMultiplePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultMap ->
            var permissionGranted = false
            resultMap.forEach {
                if (it.value == true) {
                    permissionGranted = it.value
                } else {
                    permissionGranted = false
                    return@forEach
                }
            }
            if (permissionGranted) {
                invokeCamera()
            } else {
                Toast.makeText(this, "Unable to load camera without permission.", Toast.LENGTH_LONG)
                    .show()
            }
        }

     fun takePhoto() {
        if (hasCameraPermission() == PERMISSION_GRANTED && hasExternalStoragePermission() == PERMISSION_GRANTED) {
            invokeCamera()
        } else {
            requestMultiplePermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            )
        }
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("camera", "Permission previously granted.")
                cameraViewModel.permissionGrantedCamera.value = true
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> Log.i("camera", "Show camera permissions dialog")
            else -> requestSinglePermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
