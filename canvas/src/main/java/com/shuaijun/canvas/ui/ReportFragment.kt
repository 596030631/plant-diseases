package com.shuaijun.plant.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.shuaijun.plant.databinding.FragmentReportBinding
import java.io.File


class ReportFragment : Fragment() {

    private lateinit var binding: FragmentReportBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.backButton.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ReportFragment()
    }

    override fun onResume() {
        super.onResume()
        binding.imgReport.visibility = View.VISIBLE
        binding.imgReport.setImageURI(Uri.fromFile(File("/sdcard/Android/media/com.shuaijun.plant/plant/a.png")))
    }
}