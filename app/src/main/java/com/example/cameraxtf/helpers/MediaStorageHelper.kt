package com.example.cameraxtf.helpers

import android.content.ContentValues
import android.provider.MediaStore
import android.graphics.Bitmap
import android.os.Build
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.OutputStream
import java.io.IOException

import java.io.File
import java.io.FileInputStream

fun savePhotoToGallery(context: Context, bitmap: Bitmap) {
    // Create the content values for the image to be saved
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")  // Unique filename
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraXTF") // Folder name in the Pictures directory
    }

    // Get the content resolver and insert the image into MediaStore
    val resolver = context.contentResolver
    val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        try {
            val outputStream: OutputStream? = resolver.openOutputStream(it)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            } // Compress and save the image
            outputStream?.close()
        } catch (e: IOException) {
            Log.e("Camera", "Error saving photo: $e")
        }
    }
}

fun saveVideoToGallery(context: Context, videoFile: File) {
    // Create the content values for the video to be saved
    val contentValues = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, "video_${System.currentTimeMillis()}.mp4") // Unique filename
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraXTF") // Folder name in the Movies directory
    }

    // Get the content resolver and insert the video into MediaStore
    val resolver = context.contentResolver
    val uri: Uri? = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        try {
            val outputStream: OutputStream? = resolver.openOutputStream(it)
            val inputStream = FileInputStream(videoFile)
            inputStream.copyTo(outputStream!!)
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            Log.e("Camera", "Error saving video: $e")
        }
    }
}
