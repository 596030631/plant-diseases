package com.shuaijun.plant

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.shuaijun.plant.databinding.ActivityMainBinding
import com.shuaijun.plant.snpe.SnpeTaskService
import com.shuaijun.plant.util.FLAGS_FULLSCREEN
import kotlinx.android.synthetic.main.fragment_login.*
import java.io.File

const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"
private const val IMMERSIVE_FLAG_TIMEOUT = 500L

class MainActivity : FragmentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainModel: MainViewModel
    private var snpeService: ISnpeService? = null
    private var serviceConnect: ServiceConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.addLogAdapter(AndroidLogAdapter())
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainModel = ViewModelProvider(this).get(MainViewModel::class.java)

        mainModel.setFullscreen(window, true, false)


        mainModel.initSharedPreference(this)

        mainModel.fragmentManager = supportFragmentManager

//        bindService()
    }

//    private fun bindService() {
//        serviceConnect = object : ServiceConnection {
//            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
////                Logger.d("连接到服务端")
//                snpeService = ISnpeService.Stub.asInterface(service)
//                mainModel.analysisImage.observe(this@MainActivity, {
//                    snpeService?.putTask(it.id, it.path)
//                })
//                snpeService?.observer(object : IResult.Stub() {
//                    override fun analysis(id: Long, result: String) {
//                        mainModel.analysisImageResult.postValue(AIResult(id, result))
//                    }
//                })
//            }
//
//            override fun onServiceDisconnected(name: ComponentName?) {
//                Logger.d("服务端断开")
//            }
//        }
//        val intent = Intent(this, SnpeTaskService::class.java)
//        Intent().apply {
//            `package` = "com.shuaijun.plant"
//            type = "snpe"
//            component =
//                ComponentName("com.shuaijun.plant", "com.shuaijun.plant.snpe.SnpeTaskService")
//        }
//        bindService(intent, serviceConnect ?: return, BIND_AUTO_CREATE)
//    }

    override fun onResume() {
        super.onResume()
        // Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
        // be trying to set app to immersive mode before it's ready and the flags do not stick
        binding.root.postDelayed({
            binding.root.systemUiVisibility = FLAGS_FULLSCREEN
        }, IMMERSIVE_FLAG_TIMEOUT)
    }

    /** When key down event is triggered, relay it via local broadcast so fragments can handle it */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val intent = Intent(KEY_EVENT_ACTION).apply { putExtra(KEY_EVENT_EXTRA, keyCode) }
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnect ?: return)
    }

    companion object {
        private const val tag = "MainActivity"
        private const val FACE_PERMISSION_QUEST_CAMERA = 1024
        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }

}