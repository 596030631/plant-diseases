package com.shuaijun.plant

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shuaijun.plant.snpe.SnpeTaskService

class MainViewModel() : ViewModel() {

    var analysisImage = MutableLiveData<String>() // 图片识别

    var analysisImageResult = MutableLiveData<String>() // 图片识别

    lateinit var fragmentManager: FragmentManager


}