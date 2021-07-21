package com.shuaijun.rubbish.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kaopiz.kprogresshud.KProgressHUD
import com.shuaijun.rubbish.databinding.ActivityLoginBinding
import com.shuaijun.rubbish.databinding.ItemLoginBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
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


        binding.btnEntre.setOnClickListener {
            loading.show()
            Single.timer(1300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    loading.dismiss()
                    Intent(this, FullscreenActivity::class.java).apply {
                        startActivity(this)
                    }
                },{

                })
        }

        val list = mutableListOf<ItemContent>()
        list.add(ItemContent(servers[Random.nextInt(servers.size)], true))

    }

    override fun onResume() {
        super.onResume()
        full()
    }

    private fun full() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

}