package com.shuaijun.rubbish.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaopiz.kprogresshud.KProgressHUD
import com.shuaijun.rubbish.databinding.ActivityLoginBinding
import com.shuaijun.rubbish.databinding.ItemLoginBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Consumer
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class ItemContent(var server: String, var select: Boolean)

class LoginActivity : AppCompatActivity() {

    val servers = arrayListOf(
        "10.10.9.1",
        "10.10.9.2",
        "10.10.9.3",
        "10.10.9.4",
        "10.10.9.5",
        "10.10.9.6",
        "10.10.9.7",
        "10.10.9.8",
        "10.10.9.9",
        "10.10.9.10",
        "10.10.9.11",
        "10.10.9.12",
        "10.10.9.13"
    )

    private lateinit var binding: ActivityLoginBinding
    private lateinit var adapter: Adapter<ItemContent, ItemLoginBinding>
    private lateinit var loading: KProgressHUD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loading = KProgressHUD.create(this)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setLabel("please wait")
            .setDetailsLabel("正在连接")
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)

        val list = mutableListOf<ItemContent>()
        list.add(ItemContent(servers[Random.nextInt(servers.size)], true))
//        binding.recyclerview.layoutManager = LinearLayoutManager(this)
//
//        adapter = Adapter(list, { v, p ->
//            v.server.text = list[p].server
//            if (list[p].select) {
//                v.connect.text = "已连接"
//            } else {
//                v.connect.text = ""
//            }
//            v.root.setOnClickListener {
//                loading.show()
//                Single.timer(1500, TimeUnit.MILLISECONDS)
//                    .map {
//                        for (i in list.indices) {
//                            list[i].select = i == p
//                        }
//                    }
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(Consumer {
//                        loading.dismiss()
//                        adapter.notifyDataSetChanged()
//                    })
//            }
//        }, { p ->
//            ViewHolder(
//                ItemLoginBinding.inflate(
//                    LayoutInflater.from(p.context), p, false
//                )
//            )
//        })
//        binding.recyclerview.adapter = adapter
//        binding.refresh.setOnRefreshListener {
//            Single.timer(2, TimeUnit.SECONDS)
//                .map {
//                    list.clear()
//                    servers.forEach {
//                        if (Random.nextBoolean()) list.add(ItemContent(it, false))
//                    }
//                }
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(Consumer {
//                    adapter.notifyDataSetChanged()
//                    binding.refresh.isRefreshing = false
//                })
//        }
    }

    override fun onStart() {
        super.onStart()

    }

}