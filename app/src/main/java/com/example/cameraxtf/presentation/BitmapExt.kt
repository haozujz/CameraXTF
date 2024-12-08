package com.example.cameraxtf.presentation

import android.graphics.Bitmap
import android.util.Log
//import android.widget.Toast
//import android.content.Context

fun Bitmap.centerCrop(desiredWidth: Int, desiredHeight: Int): Bitmap? {
//    Log.d("BitmapCenterCrop", "Original Bitmap dimensions: width=$width, height=$height")
//    Original Bitmap dimensions: width=640, height=480

    val xStart = (width - desiredWidth) / 2
    val yStart = (height - desiredHeight) / 2

    if(xStart <0 || yStart < 0 || desiredWidth > width || desiredHeight > height) {
        //throw IllegalArgumentException("Invalid arguments for center cropping")

        val errorMessage = "Invalid arguments for center cropping: desiredWidth=$desiredWidth, desiredHeight=$desiredHeight, width=$width, height=$height"

        Log.e("BitmapCenterCrop", errorMessage)
        //Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        return null
    }

    return Bitmap.createBitmap(this, xStart, yStart, desiredWidth, desiredHeight)
}