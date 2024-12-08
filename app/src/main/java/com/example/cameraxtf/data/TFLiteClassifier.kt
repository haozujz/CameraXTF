package com.example.cameraxtf.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.Surface
import com.example.cameraxtf.domain.Classification
import com.example.cameraxtf.domain.Classifier
import org.tensorflow.lite.support.image.ImageProcessor//
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions//
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions//
import org.tensorflow.lite.task.vision.classifier.ImageClassifier


import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.model.Model
//import org.tensorflow.lite.delegate.FlexDelegate
import org.tensorflow.lite.flex.FlexDelegate // Changed import

import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

import android.util.Log as AndroidLog



class TFLiteClassifier (
    private val context: Context,
    private val threshold: Float = 0.5f,
    private val maxResults: Int = 1
): Classifier {
    private var classifier: ImageClassifier? = null

    private fun setupClassifier() {
        val baseOptions = BaseOptions.builder()
            .setNumThreads(2)
            .build()
        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setMaxResults(maxResults)
            .setScoreThreshold(threshold)
            .build()

        try {
            classifier = ImageClassifier.createFromFileAndOptions(
                context,
                "retina_with_metadata2.tflite",
                options
            )
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }



    private var interpreter: Interpreter? = null

//    private fun setupInterpreter() {
//        // Set interpreter options and add the Flex delegate
//        val options = Interpreter.Options().apply {
//            addDelegate(org.tensorflow.lite.flex.FlexDelegate())
//            setNumThreads(2)
//        }
//
//        try {
//            // Load the model and create the interpreter
//            val modelFile = FileUtil.loadMappedFile(context, "retina.tflite")
//            interpreter = Interpreter(modelFile, options)
//        } catch (e: IllegalStateException) {
//            e.printStackTrace()
//        }
//
//        //Check model details
//        val inputDetails = interpreter?.getInputTensor(0)?.shape()
//        val inputDataType = interpreter?.getInputTensor(0)?.dataType()
//
//        val outputDetails = interpreter?.getOutputTensor(0)?.shape()
//        val outputDataType = interpreter?.getOutputTensor(0)?.dataType()
//
//        Log.d("ModelInput", "Input shape: ${inputDetails.contentToString()}, DataType: $inputDataType")
//        Log.d("ModelOutput", "Output shape: ${outputDetails.contentToString()}, DataType: $outputDataType")
//        Log.d("ModelOutput", "Output DataType: $outputDataType")
//    }

//    override fun classify(bitmap: Bitmap, rotation: Int): List<Classification> {
//        if (interpreter == null) {
//            setupInterpreter()
//        }
//
//        val imageProcessor = ImageProcessor.Builder().build()
//        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))
//
//        val imageProcessingOptions = ImageProcessingOptions.builder()
//            .setOrientation(getOrientationFromRotation(rotation))
//            .build()
//
//        // Prepare the input tensor for the model
//        val inputBuffer = preprocessImage(bitmap)
//
//        // Prepare the output buffer
//        val outputBuffer = ByteBuffer.allocateDirect(4 * Float.SIZE_BYTES) // Corrected: using Float.SIZE_BYTES
//        outputBuffer.order(ByteOrder.nativeOrder())
//
//        // Run inference
//        interpreter?.run(inputBuffer, outputBuffer)
//
//        // Process the output
//        val outputArray = FloatArray(4)
//        outputBuffer.rewind()
//        for (i in 0 until 4) {
//            outputArray[i] = outputBuffer.getFloat()
//        }
//
//        val classLabels = listOf("Cat", "Dog", "Bird", "Fish") // Replace with actual labels
//
//        fun softmax(logits: FloatArray): FloatArray {
//            val expScores = logits.map { Math.exp(it.toDouble()) }
//            val sumExpScores = expScores.sum()
//            return expScores.map { (it / sumExpScores).toFloat() }.toFloatArray()
//        }
//
//
//// Apply softmax to normalize scores
//        val normalizedScores = softmax(outputArray)
//
//        print(normalizedScores)
//
//        return normalizedScores.mapIndexed { index, score ->
//            Classification(
//                name = classLabels.getOrElse(index) { "Unknown Class" },
//                score = score
//            )
//        }
//
//    }

//    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
//        // Prepare a ByteBuffer for the input tensor
//        val inputBuffer = ByteBuffer.allocateDirect(1 * 150 * 150 * 3 * 4) // FLOAT32 -> 4 bytes
//        inputBuffer.order(ByteOrder.nativeOrder())
//
//        // Normalize pixel values to [-1, 1] and convert to FLOAT32
//        val intValues = IntArray(150 * 150)
//        bitmap.getPixels(intValues, 0, 150, 0, 0, 150, 150)
//        for (pixelValue in intValues) {
//            val r = ((pixelValue shr 16) and 0xFF) / 255.0f
//            val g = ((pixelValue shr 8) and 0xFF) / 255.0f
//            val b = (pixelValue and 0xFF) / 255.0f
//            inputBuffer.putFloat(r * 2 - 1) // Normalize to [-1, 1]
//            inputBuffer.putFloat(g * 2 - 1)
//            inputBuffer.putFloat(b * 2 - 1)
//        }
//
//        return inputBuffer
//    }

    //    private fun prepareInputBitmap(bitmap: Bitmap): ByteBuffer {
//        val width = 150
//        val height = 150
//        val buffer = ByteBuffer.allocateDirect(width * height * 3 * 4) // 4 bytes per float
//        buffer.order(ByteOrder.nativeOrder())
//
//        for (y in 0 until height) {
//            for (x in 0 until width) {
//                val pixel = bitmap.getPixel(x, y)
//                buffer.putFloat((pixel shr 16 and 0xFF) / 255.0f) // Red
//                buffer.putFloat((pixel shr 8 and 0xFF) / 255.0f)  // Green
//                buffer.putFloat((pixel and 0xFF) / 255.0f)        // Blue
//            }
//        }
//        return buffer
//    }


    override fun classify(bitmap: Bitmap, rotation: Int): List<Classification> {
        if (classifier == null) {
            setupClassifier()
        }

        // Resize and normalize the image before passing it to the classifier
        val inputBuffer = normalizeImage(bitmap, 150)

        // Convert the processed input image to TensorImage for classification
        val tensorImage = TensorImage.fromBitmap(inputBuffer)

        // Image processing options like rotation or orientation
        val imageProcessingOptions = ImageProcessingOptions.builder()
            .setOrientation(getOrientationFromRotation(rotation))
            .build()

        // Classify the image using the classifier
        val results = classifier?.classify(tensorImage, imageProcessingOptions)

        AndroidLog.d("InputBuffer", "Bitmap size: ${bitmap.width}x${bitmap.height}")
        AndroidLog.d("TensorImage", "TensorImage shape: ${tensorImage.tensorBuffer.shape}")
        AndroidLog.d("ClassifierOutput", "Results: $results")

//        val pixel = tensorImage.tensorBuffer.getFloatArray()
//        AndroidLog.d("NormalizedPixel", "First few pixels: ${pixel.take(10)}")


        // Map the classification results to a list of Classification objects for landmarks.tflite
//        return results?.flatMap { classification ->
//            classification.categories.map { category ->
//                Classification(
//                    name = category.displayName,
//                    score = category.score
//                )
//            }
//        }?.distinctBy { it.name } ?: emptyList()  // Removes duplicates outputted by model

        // Map the classification results to a list of Classification objects for retina_with_metadata.tflite
        return results?.flatMap { classification ->
            classification.categories.map { category ->
                // Check if label or displayName is used for category name
                Classification(
                    name = category.label ?: category.displayName,  // Use label or displayName based on your model
                    score = category.score
                )
            }
        }?.distinctBy { it.name }?.sortedByDescending { it.score } ?: emptyList()
    }

    fun normalizeImage(bitmap: Bitmap, targetSize: Int): Bitmap {
        // Resize the image to the target size
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, false)

        // Create a mutable bitmap to store normalized values
        val normalizedBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)

        for (x in 0 until targetSize) {
            for (y in 0 until targetSize) {
                val pixel = resizedBitmap.getPixel(x, y)

                // Extract RGB components
                val r = (pixel shr 16 and 0xFF) / 255.0f // Normalize to [0, 1]
                val g = (pixel shr 8 and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f

                // Convert back to [0, 255] range for Bitmap storage
                val finalR = (r * 255).toInt()
                val finalG = (g * 255).toInt()
                val finalB = (b * 255).toInt()

                // Reconstruct the pixel and set it in the normalized bitmap
                val normalizedPixel = (0xFF shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
                normalizedBitmap.setPixel(x, y, normalizedPixel)
            }
        }

        return normalizedBitmap
    }


    private fun getOrientationFromRotation(rotation: Int): ImageProcessingOptions.Orientation {
        return when(rotation) {
            Surface.ROTATION_0 -> ImageProcessingOptions.Orientation.RIGHT_TOP
            Surface.ROTATION_90 -> ImageProcessingOptions.Orientation.TOP_LEFT
            Surface.ROTATION_180 -> ImageProcessingOptions.Orientation.LEFT_BOTTOM
            Surface.ROTATION_270 -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
            else -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
        }
    }

    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        // Prepare a ByteBuffer for the input tensor
        val inputBuffer = ByteBuffer.allocateDirect(1 * 150 * 150 * 3 * 4) // FLOAT32 -> 4 bytes
        inputBuffer.order(ByteOrder.nativeOrder())

        // Normalize pixel values to [0, 1] and convert to FLOAT32 (You can adjust this based on your model)
        val intValues = IntArray(150 * 150)
        bitmap.getPixels(intValues, 0, 150, 0, 0, 150, 150)
        for (pixelValue in intValues) {
            val r = ((pixelValue shr 16) and 0xFF) / 255.0f
            val g = ((pixelValue shr 8) and 0xFF) / 255.0f
            val b = (pixelValue and 0xFF) / 255.0f
            inputBuffer.putFloat(r)  // Normalize to [0, 1]
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }

        return inputBuffer
    }

}
