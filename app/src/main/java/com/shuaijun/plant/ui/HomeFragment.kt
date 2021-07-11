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
import com.shuaijun.plant.databinding.ItemHomeContentBinding
import com.shuaijun.plant.databinding.ItemHomeTabBinding
import com.shuaijun.plant.util.Adapter
import com.shuaijun.plant.util.ViewHolder
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.databinding.ItemSample1Binding
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator
import com.youth.banner.transformer.ScaleInTransformer

data class TabItem(val name: String, var select: Boolean)

class HomeFragment : BaseFragment() {

    private lateinit var viewModel: HomeViewModel

    private lateinit var binding: HomeFragmentBinding
    private lateinit var contents: MutableList<Content>
    private lateinit var tabAdapter: Adapter<TabItem, ItemHomeTabBinding>
    private lateinit var contentAdapter: Adapter<ContentData, ItemHomeContentBinding>

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

    data class ContentData(var title: String, var text: String)

    private val contentData = mutableListOf<ContentData>(
        ContentData(
            "中国科学院发布我国首部东北黑土地白皮书",
            "7月9日，中国科学院在黑龙江哈尔滨市发布《东北黑土地白皮书（2020）》，这是我国首次发布黑土地白皮书。中国科学院希望通过黑土地白皮书的发布，为“用好养好”黑土地提供科技支撑，同时能够让全社会对黑"
        ),
        ContentData(
            "农业农村部印发《意见》提出 以农业社会化服务更好地引领 小农户和农业现",
            "近年来，各级各部门深入贯彻中央决策部署，加强引导推动，农业社会化服务在探索创新中蓬勃发展，成为稳定粮食等大宗农产品生产的重要支撑。但与现代农业发展的要求相比，还面临规模不大、领域不宽、质量"
        ),
        ContentData(
            "7月份CPI同比上涨1.1% 环比下降0.4%",
            "8月9日，国家统计局发布6月份全国CPI（居民消费价格指数）数据。数据显示，6月份，全国居民消费价格同比上涨1.1%。上半年，全国居民消费价格比去年同期上涨0.5%。6月份，全国居民消费价格环比下降0"
        ),
        ContentData(
            "第三次中韩联合增殖放流活动在中国烟台和韩国仁川同步举行\n" +
                    "为保护黄海渔业资源和生态环境，根据中韩渔委会会谈达成共识",
            "7月9日，第三次中韩联合增殖放流活动通过线上线下相结合方式，在中国山东烟台和韩国仁川同步举行，共向黄海投放绿鳍马面鲀、黑鲷、小黄鱼"
        ),
        ContentData(
            "农业农村部部署“双抢”早稻机收减损工作",
            "南方早稻将于7月中旬进入集中收获期。日前,农业农村部对双季稻产区早稻机收减损工作作出部署，要求各级农业农村部门牢固树立“减损就是增产”意识，将机收减损作为“双抢”机械化生产工作的重中之重，由厅局一"
        ),
        ContentData(
            "国家矿山安监局召开新闻发布会： 以大概率思维应对小概率事件，防范遏制矿",
            "7月9日，国家矿山安监局召开新闻发布会，通报今年上半年全国矿山安全生产形势和重点工作进展情况，并回答记者提问。据介绍，在党中央、国务院的坚强领导下，国家矿山安监局以防范遏制重特大事故为“牛鼻"
        ),
        ContentData(
            "《乡村振兴促进法专刊》出版",
            "为指导全国各地学习贯彻乡村振兴促进法，日前，农业农村部法规司、中国农村杂志社编辑出版《农村工作通讯·乡村振兴促进法专刊》。该专刊包括卷首、特稿、权威解读、专家说法、地方法治五大板块。特稿板块"
        ),
        ContentData(
            "农业农村部部署“双抢”早稻机收减损工作",
            "南方早稻将于7月中旬进入集中收获期。日前,农业农村部对双季稻产区早稻机收减损工作作出部署，要求各级农业农村部门牢固树立“减损就是增产”意识，将机收减损作为“双抢”机械化生产工作的重中之重，由厅局一"
        ),
        ContentData(
            "中国消除绝对贫困助力全球包容发展",
            "中国全面建成小康社会，历史性地解决了绝对贫困问题，书写了人类发展史上的伟大奇迹。海外观察人士认为，中国共产党带领人民打赢脱贫攻坚战、消除绝对贫困，不仅对中国自身发展意义重大，也将有力推动世"
        ),
        ContentData(
            "解读：2021年6月CPI和PPI同比涨幅均略有回落",
            "国家统计局今天发布了2021年6月份全国CPI（居民消费价格指数）和PPI（工业生产者出厂价格指数）数据。对此，国家统计局城市司高级统计师董莉娟进行了解读。"
        ),
        ContentData("联合国粮农组织：全球食品价格一年来首次环比回落", "联合国粮食及农业组织8日发布的月度报告显示，6月全球食品价格在连续12个月上涨后首次环比回落。"),
        ContentData(
            "国家缘何对猪肉进行临时收储？对猪价走势有何影响？",
            "7月7日，商务部和国家发展改革委、财政部等有关部门启动2021年度第一批中央储备猪肉收储，总量为2万吨。"
        ),
        ContentData(
            "2021年6月CPI同比上涨1.1% 环比下降0.4%",
            "2021年6月份，全国居民消费价格同比上涨1.1%。其中，城市上涨1.2%，农村上涨0.7%；食品价格下降1.7%，非食品价格上涨1.7%；消费品价格上涨1.1%，服务价格上涨1.0%。上半年，全国居民消费价格比去"
        ),
        ContentData(
            "商务部：受汛情影响地区生活必需品市场稳定",
            "新华社北京7月8日电（记者王雨萧、于佳欣）商务部新闻发言人高峰8日介绍，根据商务部监测，目前有关受汛情影响的地区生活必需品市场稳定，供应充足，可以满足居民日常消费需求。高峰在商务部当天举行的"
        ),
        ContentData(
            "306种版本《共产党宣言》亮相国图",
            "新华社北京7月8日电（记者余俊杰）由国家图书馆与中共浙江省委宣传部共同主办的《共产党宣言》专题展8日在国家图书馆开幕，展出中文、德文、俄文、英文、法文等55种语言的《共产党宣言》版本达306种"
        ),
        ContentData(
            "中国已向国际社会供应超4.8亿剂次疫苗",
            "新华社北京7月8日电（记者沐铁城、马卓言）当前，全球新冠疫情形势依然严峻，全球疫苗接种不平衡问题十分突出。推动抗疫国际合作，中国始终坚定秉持疫苗全球公共产品的“第一属性”。目前，中国已尽己所..."
        ),
        ContentData(
            "4个重点！2021深化医改将这样开展",
            "新华社记者彭韵佳、沐铁城国家卫健委、国家医保局等部门有关负责人在8日举行的国务院政策例行吹风会上表示，2021年深化医改工作将围绕4个重点开展，着力解决看病难、看病贵问题，突出推广医改典型经"
        ),


        )

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

        initRecyclerView()
    }

    private fun initTab() {
        binding.tabs.layoutManager = LinearLayoutManager(requireContext()).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }

        tabAdapter = Adapter(tagList, { v, p ->
            v.label.text = tagList[p].name
            v.label.setTextColor(if (tagList[p].select) Color.RED else Color.BLACK)
            v.label.setOnClickListener {
                tagList[lastTabItem].select = false
                lastTabItem = p
                tagList[p].select = true
                tabAdapter.notifyDataSetChanged()
                for (i in 0..contentData.size - 5) {
                    contentData[i] = contentData[i + 4]
                }
                contentAdapter.notifyDataSetChanged()
            }

        }, { p ->
            ViewHolder(
                ItemHomeTabBinding.inflate(LayoutInflater.from(p.context), p, false)
            )
        })
        binding.tabs.adapter = tabAdapter
    }

    private fun initAdvert() {

        binding.banner.setAdapter(object : BannerImageAdapter<DataBean?>(DataBean.getTestData()) {
            override fun onBindView(
                holder: BannerImageHolder<ItemSample1Binding>?,
                data: DataBean?,
                position: Int,
                size: Int
            ) {
                holder?.let {
                    data?.let { d ->
                        Glide.with(it.itemView)
                            .load(d.imageRes)
                            .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                            .into(holder.binding.image)
                        holder.binding.title.setText(d.title)
                    }

                }
            }
        })
//            .setBannerGalleryEffect(20, 20, 0.8f)
//            .addPageTransformer(RotateDownPageTransformer())
            .addPageTransformer(ScaleInTransformer())
            .addBannerLifecycleObserver(this).indicator = CircleIndicator(requireContext())
    }

    private fun initRecyclerView() {
        contentAdapter = Adapter(
            contentData,
            { v, p ->
                v.text.text = contentData[p].text
                v.title.text = contentData[p].title
            },
            { p ->
                ViewHolder(
                    ItemHomeContentBinding.inflate(
                        LayoutInflater.from(p.context),
                        p,
                        false
                    )
                )
            }
        )
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerview.adapter = contentAdapter
    }

    override fun onStart() {
        super.onStart()
        binding.banner.start()

        mainModel.setFullscreen(requireActivity().window, true, true)

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