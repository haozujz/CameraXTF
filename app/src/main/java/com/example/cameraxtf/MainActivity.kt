@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cameraxtf

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cameraxtf.ui.theme.CameraXTFTheme

import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoCamera
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy

import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException

import android.util.Log
import androidx.compose.material.icons.filled.Photo

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

import android.graphics.Matrix
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberBottomSheetScaffoldState

import androidx.compose.material.icons.filled.Videocam
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.video.AudioConfig
import java.io.File
import android.widget.Toast
import com.example.cameraxtf.helpers.savePhotoToGallery
import com.example.cameraxtf.helpers.saveVideoToGallery
import com.example.cameraxtf.presentation.CameraPreview
import com.example.cameraxtf.viewmodels.MainViewModel

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.cameraxtf.data.TFLiteClassifier
import com.example.cameraxtf.domain.Classification
import com.example.cameraxtf.presentation.ImageAnalyzer
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.size

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.border


class MainActivity : ComponentActivity() {

    private var recording: Recording? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(
                this, CAMERAX_PERMISSIONS, 0
            )
        }

        setContent {
            CameraXTFTheme {
                // Use mutableStateOf directly to create a mutable state
                val classifications = remember { mutableStateOf(emptyList<Classification>()) }

                // Initialize ImageAnalyzer with the onResults callback to update classifications
                val analyzer = remember {
                    ImageAnalyzer(
                        classifier = TFLiteClassifier(
                            context = applicationContext
                        ),
                        onResults = {
                            classifications.value = it  // Use .value to update the state
                        }
                    )
                }
                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        // Enable all required use cases: IMAGE_ANALYSIS, IMAGE_CAPTURE, VIDEO_CAPTURE
                        //setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
                        setEnabledUseCases(
                            CameraController.IMAGE_ANALYSIS or
                                    CameraController.IMAGE_CAPTURE or
                                    CameraController.VIDEO_CAPTURE
                        )

                        // Set up the analyzer for image classification
                        setImageAnalysisAnalyzer(
                            ContextCompat.getMainExecutor(applicationContext),
                            analyzer
                        )
                    }
                }

                // Capture photo or Video
//                val controller = remember {
//                    LifecycleCameraController(applicationContext).apply {
//                        setEnabledUseCases(
//                            CameraController.IMAGE_CAPTURE or
//                                    CameraController.VIDEO_CAPTURE
//                        )
//                    }
//                }

                val scaffoldState = rememberBottomSheetScaffoldState()
                val viewModel = viewModel<MainViewModel>()
                val bitmaps by viewModel.bitmaps.collectAsState()
                val scope = rememberCoroutineScope()

                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {
                        PhotoBottomSheetContent(
                            bitmaps = bitmaps,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        CameraPreview(
                            controller = controller,
                            modifier = Modifier
                                .fillMaxSize()
                        )

                        // Add the overlay with a square for center crop
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                        ) {
                            val cropSize = 321 // Size of the center crop area
                            val offset = (LocalDensity.current.density * (480 - 321)) / 2 // Centering the square
                            Box(
                                modifier = Modifier
                                    .width(cropSize.dp)
                                    .height(cropSize.dp)
                                    .border(2.dp, Color.Blue)
                                    .align(Alignment.Center)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(top = 38.dp)
                        ) {
                            classifications.value.forEach {
                                Text(
                                    text = it.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(8.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Display the confidence score underneath the classification name
                                Text(
                                    text = "Confidence: ${"%.2f".format(it.score)}",  // Format the score to 2 decimal places
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(top = 4.dp),  // Add some space between the name and score
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                controller.cameraSelector =
                                    if(controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(bottom = 76.dp, start = 28.dp)

                        ) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Switch camera",
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        scaffoldState.bottomSheetState.expand()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Photo,
                                    contentDescription = "Open gallery"
                                )
                            }

                            IconButton(
                                onClick = {
                                    takePhoto(
                                        controller = controller,
                                        onPhotoTaken = viewModel::onTakePhoto
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Take Photo"
                                )
                            }

                            IconButton(
                                onClick = {
                                    recordVideo(controller)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "Record Video"
                                )
                            }

                        }
                    }
                }

            }
        }

    }

    private fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap) -> Unit
    ) {
        if(!hasRequiredPermissions()) {
            return
        }

        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                        postScale(-1f, 1f)
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true
                    )

                    onPhotoTaken(rotatedBitmap)

                    savePhotoToGallery(applicationContext, rotatedBitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Couldn't take photo: ", exception)
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun recordVideo(controller: LifecycleCameraController) {
        if(recording != null) {
            recording?.stop()
            recording = null
            return
        }

        if(!hasRequiredPermissions()) {
            return
        }

        //This file will replaced by each new recording
        val outputFile = File(filesDir, "recording-cxtf.mp4")
        recording = controller.startRecording(
            FileOutputOptions.Builder(outputFile).build(),
            AudioConfig.create(true),
            ContextCompat.getMainExecutor(applicationContext),
        ) { event ->
            when(event) {
                is VideoRecordEvent.Finalize -> {
                    if(event.hasError()) {
                        recording?.close()
                        recording = null

                        Toast.makeText(
                            applicationContext,
                            "Video capture failed",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Video capture succeeded",
                            Toast.LENGTH_LONG
                        ).show()

                        saveVideoToGallery(applicationContext, outputFile)
                    }
                }
            }
        }

    }

    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CameraXTFTheme {
        Greeting("Android")
    }
}