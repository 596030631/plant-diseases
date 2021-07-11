package com.shuaijun.rubbish

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class ImageInputData(val id: Long, var path: String, var info: String)

data class AIResult(val id: Long, val result: String)

class MainViewModel() : ViewModel() {

    var analysisImage = MutableLiveData<ImageInputData>() // 图片识别

    var analysisImageResult = MutableLiveData<AIResult>() // 图片识别


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

    fun setFullscreen(window: Window, isShowStatusBar: Boolean, isShowNavigationBar: Boolean) {
        var uiOptions: Int = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        if (!isShowStatusBar) {
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        if (!isShowNavigationBar) {
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
        window.decorView.systemUiVisibility = uiOptions
        setNavigationStatusColor(window, Color.TRANSPARENT)
    }

    private fun setNavigationStatusColor(window: Window, color: Int) {
        //VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = color
        window.statusBarColor = color
    }

}