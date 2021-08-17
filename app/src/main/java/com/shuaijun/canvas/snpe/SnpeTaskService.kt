package com.shuaijun.canvas.snpe

import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import com.orhanobut.logger.Logger
import com.shuaijun.canvas.IResult
import com.shuaijun.canvas.ISnpeService

class SnpeTaskService : Service() {

    private val handlerThread = HandlerThread("snpe")
    private lateinit var handler: Handler

    override fun onCreate() {
        super.onCreate()
        Logger.d("服务端创建")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        LoadModelTask.getInstance().loadNetwork(application)
    }

    override fun onBind(intent: Intent): IBinder {
        Logger.d("服务端绑定")
        return binder
    }

    private val binder = object : ISnpeService.Stub() {

        override fun observer(li: IResult?) {
            Logger.d("服务端设置回调")
            listener = li
        }

        override fun putTask(id: Long, pathName: String?) {
            if (ImageDetectionFloat.getInstance().available()) {
                handler.post {
                    val bitmap = BitmapFactory.decodeFile(pathName)
                    bitmap?.let { bp ->
                        ImageDetectionFloat.getInstance().detection(bp) {
                            Logger.w(it.toString())
                            listener?.analysis(id, it.toString())
                        }
                    }
                }
            } else {
                Logger.d("识别任务繁忙")
            }
        }
    }

    companion object {
        var listener: IResult? = null
    }
}