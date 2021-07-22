package com.shuaijun.rubbish.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.shuaijun.rubbish.MainViewModel
import com.shuaijun.rubbish.databinding.FragmentHistoryBinding
import com.shuaijun.rubbish.databinding.ItemHistoryInfoBinding

data class DataInfo(
    val time: String,
    val image: String,
    val name: String,
    val category: String,
    val bias: String
)

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private val mainModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentHistoryBinding.inflate(inflater, container, false).apply {
        binding = this
    }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.exit.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        binding.recyclerview.layoutManager = GridLayoutManager(requireContext(), 2).apply {
            orientation = GridLayoutManager.HORIZONTAL
        }
        binding.recyclerview.adapter =
            Adapter(mainModel.mutableDataInfoList,
                { v, p ->
                    v.time.text = "时间：${mainModel.mutableDataInfoList[p].time}"
                    v.name.text = "名称：${mainModel.mutableDataInfoList[p].name}"
                    v.category.text = "类别：${mainModel.mutableDataInfoList[p].category}"
                    v.bias.text = "置信：${mainModel.mutableDataInfoList[p].bias}"
                    v.image.setImageBitmap(BitmapFactory.decodeFile(mainModel.mutableDataInfoList[p].image))
                }, { p ->
                    ViewHolder(
                        ItemHistoryInfoBinding.inflate(LayoutInflater.from(p.context), p, false)
                    )
                })
    }


    companion object {

        @JvmStatic
        fun newInstance() =
            HistoryFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}