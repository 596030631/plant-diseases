package com.shuaijun.canvas.view

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.util.AttributeSet
import android.view.TextureView

class PointView : TextureView, TextureView.SurfaceTextureListener {

    private var paint: Paint = Paint()
    private lateinit var pointHandler: Handler
    private var release = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

    }

    private val pointRunnable = Runnable {
        while (!release) {
            lockCanvas()?.let { canvas ->
                canvas.drawPoint(50f, 50f, paint)
                unlockCanvasAndPost(canvas)
            }
            SystemClock.sleep(100)
        }
    }

    init {
        paint.strokeWidth = 10f
        paint.setColor(Color.RED)
        val handlerThread = HandlerThread("point")
        handlerThread.start()
        pointHandler = Handler(handlerThread.looper)
        pointHandler.post(pointRunnable)

    }


    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        release = true
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }


}