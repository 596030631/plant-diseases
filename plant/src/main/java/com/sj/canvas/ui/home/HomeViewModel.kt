package com.sj.canvas.ui.home

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }

    val text: LiveData<String> = _text

    fun setNullImage(_bitmap: Bitmap) {
        if (bitmap.value == null) bitmap.value = _bitmap
    }

    val bitmap: MutableLiveData<Bitmap> = MutableLiveData()
}