package com.example.cameraxtf.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel: ViewModel() {

    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps = _bitmaps.asStateFlow()    //public version of 'private val _bitmaps'

    fun onTakePhoto(bitmap: Bitmap) {
        _bitmaps.value += bitmap
    }
}