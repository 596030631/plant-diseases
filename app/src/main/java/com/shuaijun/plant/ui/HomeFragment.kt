package com.shuaijun.plant.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.shuaijun.plant.data.Content
import com.shuaijun.plant.databinding.HomeFragmentBinding
import com.shuaijun.plant.databinding.ItemContentBinding
import com.shuaijun.plant.util.Adapter

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
            Navigation.findNavController(it)
                .navigate(HomeFragmentDirections.actionHomeFragmentToCameraFragment())
        }
        contents = Content.loadFromFile()
    }

    override fun onStart() {
        super.onStart()
        binding.webview.loadUrl("file:///android_asset/a.html")
        binding.webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                return super.shouldOverrideUrlLoading(view, url)
            }
        }
//        binding.webview.loadUrl("https://baijiahao.baidu.com/s?id=1704531400566012093&wfr=spider&for=pc")
//        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext()).also {
//            it.orientation = LinearLayoutManager.HORIZONTAL
//        }
//
//        adapter = Adapter(
//            contents,
//            { v, p ->
//                v.title.text = contents[p].title
//                v.content.text = contents[p].content
//            },
//            { parent ->
//                ViewHolder(
//                    ItemContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//                )
//            })
//
//        binding.recyclerview.adapter = adapter
    }

    companion object {
        const val TAG = "HomeFragment"
        const val id = 1

        @JvmStatic
        fun newInstance() = HomeFragment()
    }

}