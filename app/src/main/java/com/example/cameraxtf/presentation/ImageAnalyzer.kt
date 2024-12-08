package com.example.cameraxtf.presentation

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.cameraxtf.domain.Classification
import com.example.cameraxtf.domain.Classifier
import android.graphics.Bitmap

// Covert image into model input format
class ImageAnalyzer(
    private val classifier: Classifier,
    private val onResults: (List<Classification>) -> Unit
): ImageAnalysis.Analyzer {
    // Skip some frames
    private var frameSkipCounter = 0

    override fun analyze(image: ImageProxy) {
        if(frameSkipCounter % 30 == 0) {
            val rotationDegrees = image.imageInfo.rotationDegrees
            val bitmap = image
                .toBitmap()
                .centerCrop(321, 321)

//            if(bitmap != null) {
//                val results = classifier.classify(bitmap, rotationDegrees)
//                onResults(results)
//            }

            // Resize the cropped bitmap to 150x150
            val resizedBitmap = bitmap?.let {
                Bitmap.createScaledBitmap(it, 150, 150, true) // Use the Bitmap class directly
            }

            if (resizedBitmap != null) {
                // Pass the resized bitmap to the classifier
                val results = classifier.classify(resizedBitmap, rotationDegrees)
                onResults(results)
            }
        }

        frameSkipCounter++
        image.close()
    }
}
