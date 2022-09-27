package com.example.kbbqreview.screens.camera

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CameraCapture(
    modifier: Modifier = Modifier,
    onImageFile: (File) -> Unit = { },
    showGallerySelect: MutableState<Boolean>,
) {
    val context = LocalContext.current

    val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    val lensFacing = remember {
        mutableStateOf(CameraSelector.LENS_FACING_BACK)
    }
    val cameraSelector2: CameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing.value).build()
    Permission(
        permission = Manifest.permission.CAMERA,
        rationale = "You said you wanted a picture, so I'm going to have to ask for permission.",
        permissionNotAvailableContent = {
            Column(modifier) {
                Text("O noes! No Camera!")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    })
                }) {
                    Text("Open Settings")
                }
            }
        }
    ) {
        Box(modifier = modifier) {
            val lifecycleOwner = LocalLifecycleOwner.current
            val coroutineScope = rememberCoroutineScope()
            var previewUseCase by remember { mutableStateOf<UseCase>(Preview.Builder().build()) }
            val imageCaptureUseCase by remember {
                mutableStateOf(
                    ImageCapture.Builder()
                        .setCaptureMode(CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build()
                )
            }
            Box {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onUseCase = {
                        previewUseCase = it
                    }
                )
                Row(Modifier
                    .background(Color.Black)
                    .padding(vertical = 40.dp, horizontal = 28.dp)

                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(
                        modifier = Modifier
                            .wrapContentSize(),
                        onClick = {
                            when (lensFacing.value) {
                                CameraSelector.LENS_FACING_FRONT ->  lensFacing.value = CameraSelector.LENS_FACING_BACK
                                CameraSelector.LENS_FACING_BACK ->  lensFacing.value = CameraSelector.LENS_FACING_FRONT
                            }

                        }
                    ) {
                        Icon(Icons.Rounded.Cameraswitch,
                            null,
                            tint = Color.White,
                            modifier = Modifier
                                .scale(2f)
                        )
                    }
                    IconButton(
                        modifier = Modifier
                            .wrapContentSize(),
                        onClick = {
                            coroutineScope.launch {
                                imageCaptureUseCase.takePicture(context.executor).let {
                                    onImageFile(it)
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.Circle,
                            null,
                            tint = Color.White,
                            modifier = Modifier
                                .scale(3f)
                                .border(1.dp, Color.White, CircleShape)
                                .padding(0.1.dp))
                    }
                    IconButton(
                        modifier = Modifier.wrapContentSize(),
                        onClick = {
                            showGallerySelect.value = true
                        }
                    ) {
                        Icon(Icons.Rounded.PhotoLibrary,
                            null,
                            tint = Color.White,
                            modifier = Modifier
                                .scale(2f))
                    }
                }

            }
            LaunchedEffect(lensFacing.value) {
                val cameraProvider = context.getCameraProvider()
                try {
                    // Must unbind the use-cases before rebinding them.
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector2, previewUseCase, imageCaptureUseCase
                    )
                } catch (ex: Exception) {
                    Log.e("CameraCapture", "Failed to bind camera use cases", ex)
                }
            }
        }
    }
}

