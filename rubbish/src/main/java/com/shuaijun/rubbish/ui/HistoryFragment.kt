package com.shuaijun.rubbish.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaopiz.kprogresshud.KProgressHUD
import com.shuaijun.rubbish.MainViewModel
import com.shuaijun.rubbish.databinding.FragmentHistoryBinding
import com.shuaijun.rubbish.databinding.ItemHistoryInfoBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

data class DataInfo(
    val time: String,
    val image: String,
    val name: String,
    val category: String,
    val bias: String
) {
    override fun toString(): String {
        return "DataInfo(time='$time', image='$image', name='$name', category='$category', bias='$bias')"
    }
}

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var loading: KProgressHUD
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

        binding.back.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

//        binding.recyclerview.layoutManager = GridLayoutManager(requireContext(), 1).apply {
//            orientation = GridLayoutManager.HORIZONTAL
//        }
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = GridLayoutManager.HORIZONTAL
        }
        loading = KProgressHUD.create(requireContext())
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setLabel("upload")
            .setDetailsLabel("上传中")
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        binding.recyclerview.adapter =
            Adapter(mainModel.mutableDataInfoList,
                { v, p ->
                    val time = "时间：" + mainModel.mutableDataInfoList[p].time
                    v.time.text = time
                    v.name.text = "名称：" + mainModel.mutableDataInfoList[p].name
                    v.category.text = "类别：${mainModel.mutableDataInfoList[p].category}"
                    v.bias.text = "置信：${mainModel.mutableDataInfoList[p].bias}"
                    v.image.setImageBitmap(BitmapFactory.decodeFile(mainModel.mutableDataInfoList[p].image))
                    v.upload.setOnClickListener {
                        loading.show()
                        Completable.timer(1200, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                loading.dismiss()
                                Toast.makeText(requireContext(), "上传完成", Toast.LENGTH_SHORT).show()
                            }
                    }
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