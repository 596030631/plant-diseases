package com.shuaijun.plant.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.orhanobut.logger.Logger
import com.shuaijun.plant.R
import com.shuaijun.plant.data.Content
import com.shuaijun.plant.databinding.HomeFragmentBinding
import com.shuaijun.plant.databinding.ItemContentBinding
import com.shuaijun.plant.util.Adapter
import com.shuaijun.plant.util.ViewHolder

class HomeFragment : BaseFragment() {

    private lateinit var viewModel: HomeViewModel

    private lateinit var binding: HomeFragmentBinding
    private lateinit var contents: MutableList<Content>
    private lateinit var adapter: Adapter<Content, ItemContentBinding>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        binding.floatingActionButton.setOnClickListener {
            mainModel.fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CameraFragment.newInstance()).commit()
        }

        contents = Content.loadFromFile()
        Logger.d("加载文件数据")
    }

    override fun onStart() {
        super.onStart()
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext()).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }

        adapter = Adapter(
            contents,
            { v, p ->
                v.title.text = contents[p].title
                v.content.text = contents[p].content
            },
            { parent ->
                ViewHolder(
                    ItemContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            })

        binding.recyclerview.adapter = adapter
    }

    companion object {
        const val TAG = "HomeFragment"
        const val id = 1

        @JvmStatic
        fun newInstance() = HomeFragment()
    }

}