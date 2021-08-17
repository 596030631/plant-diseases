package com.shuaijun.canvas.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shuaijun.canvas.MainViewModel

abstract class BaseFragment : Fragment() {

    lateinit var mainModel:MainViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }
}