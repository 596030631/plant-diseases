package com.shuaijun.plant

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shuaijun.plant.ui.ImageData

class MainViewModel() : ViewModel() {

    var analysisImage = MutableLiveData<ImageData>() // 图片识别

    var analysisImageResult = MutableLiveData<String>() // 图片识别

    lateinit var fragmentManager: FragmentManager


    private lateinit var sharedPreference: SharedPreferences

    fun initSharedPreference(ctx: Context) {
        sharedPreference = ctx.getSharedPreferences("plant", Context.MODE_PRIVATE)
    }

    fun putString(key: String, value: String) {
        sharedPreference.edit()?.putString(key, value)?.apply()
    }

    fun getString(key: String, default: String): String? {
        return sharedPreference.getString(key, default)
    }


}