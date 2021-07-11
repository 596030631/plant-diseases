package com.shuaijun.plant.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.shuaijun.plant.data.Content
import com.shuaijun.plant.data.DataBean
import com.shuaijun.plant.databinding.HomeFragmentBinding
import com.shuaijun.plant.databinding.ItemHomeTabBinding
import com.shuaijun.plant.util.Adapter
import com.shuaijun.plant.util.ViewHolder
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator
import com.youth.banner.transformer.ZoomOutPageTransformer

data class TabItem(val name: String, var select: Boolean)

class HomeFragment : BaseFragment() {

    private lateinit var viewModel: HomeViewModel

    private lateinit var binding: HomeFragmentBinding
    private lateinit var contents: MutableList<Content>
    private lateinit var adapter: Adapter<TabItem, ItemHomeTabBinding>

    private val tagList = mutableListOf(
        TabItem("推荐", true),
        TabItem("科技", false),
        TabItem("文旅", false),
        TabItem("榜样", false),
        TabItem("致富", false),
        TabItem("前沿", false),
        TabItem("案例", false),
        TabItem("动态", false),
    )
    private var lastTabItem = 0

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
        initAdvert()

        initTab()
    }

    private fun initTab() {
        binding.tabs.layoutManager = LinearLayoutManager(requireContext()).also {
            it.orientation = LinearLayoutManager.HORIZONTAL

        }

        adapter = Adapter(tagList, { v, p ->

            v.label.text = tagList[p].name
            v.label.setTextColor(if (tagList[p].select) Color.WHITE else Color.BLACK)

            v.label.setOnClickListener {
                tagList[lastTabItem].select = false
                lastTabItem = p
                tagList[p].select = true
                adapter.notifyDataSetChanged()
            }

        }, { p ->
            ViewHolder(
                ItemHomeTabBinding.inflate(LayoutInflater.from(p.context), p, false)
            )
        })
        binding.tabs.adapter = adapter
    }

    private fun initAdvert() {
        binding.banner.setAdapter(object : BannerImageAdapter<DataBean?>(DataBean.getTestData()) {
            override fun onBindView(
                holder: BannerImageHolder?,
                data: DataBean?,
                position: Int,
                size: Int
            ) {
                holder?.let {
                    data?.let { d ->
                        Glide.with(it.itemView)
                            .load(d.imageRes)
                            .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                            .into(holder.imageView)
                    }

                }
            }
        })
            .setBannerGalleryEffect(5, 5, 0.9f)
            .addBannerLifecycleObserver(this).indicator = CircleIndicator(requireContext())
    }

    override fun onStart() {
        super.onStart()
        binding.banner.start()

        mainModel.setFullscreen(requireActivity().window, false, true)

//        binding.webview.loadUrl("http://www.farmer.com.cn/")
//        binding.webview.webViewClient = object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//                view?.loadUrl(url)
//                return super.shouldOverrideUrlLoading(view, url)
//            }
//        }
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

    override fun onStop() {
        super.onStop()
        binding.banner.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.banner.destroy()
    }

    companion object {
        const val TAG = "HomeFragment"
        const val id = 1

        @JvmStatic
        fun newInstance() = HomeFragment()
    }

}