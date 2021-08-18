package com.sj.canvas

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sj.canvas.ai.LoadModelTask
import com.sj.canvas.databinding.ActivityMainBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.statusBarColor = resources.getColor(R.color.grey)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
        Completable.create {
            LoadModelTask.getInstance().loadNetwork(application)
        }.subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Toast.makeText(this, "网络加载完成", Toast.LENGTH_SHORT).show()
            }, {
                Toast.makeText(this, "网络加载异常", Toast.LENGTH_SHORT).show()
            })



//        window.decorView.also {
//            it.viewTreeObserver.addOnGlobalLayoutListener {
//                val rect = Rect()
//                it.getWindowVisibleDisplayFrame(rect)
//                val height: Int =
//                    it.context.resources.displayMetrics.heightPixels
//                val diff = height - rect.height()
//                Log.d("tag_canvas", "height:$height")
//                Log.d("tag_canvas", "rect:$rect")
//                Log.d("tag_canvas", "diff:$diff")
//
//                if (diff > 0) {
//                    if (binding.root.paddingBottom !== diff) {
//                        // 将聊天记录定位到最后一行
////                        binding.recyclerview.scrollToPosition(messageAdapter.getItemCount() - 1)
//                        binding.root.setPadding(0, 0, 0, diff)
//                    }
//                } else {
////                    if (contentView.getPaddingBottom() !== 0) {
//                    binding.root.setPadding(0, 0, 0, 0)
////                    }
//                }
//            }
//        }
    }

    override fun onResume() {
        super.onResume()
        val decorView = window.decorView
        val uiOptions = decorView.systemUiVisibility
        var newUiOptions = uiOptions
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = newUiOptions
    }

}