package com.sj.canvas.ui.chat

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.ViewModel
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors
import kotlin.random.Random

class ChatViewModel : ViewModel() {

    val listChat = mutableListOf<Content>()
    private val random = Random(System.currentTimeMillis())
    private val thread = Executors.newSingleThreadExecutor()

    init {
        listChat.add(Content(text = "我是您的专属人工助手"))
        listChat.add(Content(text = "您可以对我说:我要猜手绘"))
    }

    data class Content(var left: Boolean = true, var image: Drawable? = null, var text: String = "")

    private var imageIndex = 0

    val arrary0 = arrayOf("马上开始", "要加油哦，开始你的表演吧", "开始了，相信你是最棒的", "我要出招了")
    val arrary1 = arrayOf("好呀，马上来喽", "要加油哦", "接下来的或许可以猜中哦", "接招", "下面的可能有一点点难哦")
    val arrary2 = arrayOf("亲请换个问法", "我答不上来", "我还不太理解", "这问题好难，我会努力改进，请换个问题", "假装不在")
    val arrary3 = arrayOf("请继续坚持一下", "或许能蒙对呢", "再试试嘛", "这问题好难，但我相信你会认出来的", "难到了吧")

    fun messageResponse(context: Context, input: String) {
        when {
            input.contains("猜手绘") -> {
                listChat.add(Content(true, null, "准备好了吗，我要开始喽!"))
                responseImage(context)
            }
            input.contains("1") -> {
                listChat.add(Content(true, null, "准备好了吗，我要开始喽!"))
                responseImage(context)
            }

            input.contains("不会") -> {
                listChat.add(Content(true, null, arrary3[random.nextInt(arrary3.size)]))
            }

            input.contains("不试") -> {
                listChat.add(Content(true, null, "那好吧"))
            }

            input.contains("换") || input.contains("猜不出") -> {
                listChat.add(Content(true, null, arrary1[random.nextInt(arrary1.size)]))
                responseImage(context)
            }
            else -> Content(true, null, arrary2[random.nextInt(arrary2.size)])
        }
    }

    private fun responseImage(context: Context) {
        thread.execute {
            var i: Int
            do {
                i = random.nextInt(57)
                if (imageIndex != i) {
                    imageIndex = i
                    break
                }
            } while (true)
            Log.d("et_log", "获取图片:${imageIndex}")
            listChat.add(
                Content(
                    image = assets2Drawable(
                        context,
                        "image/$imageIndex.jpg"
                    ), text = "$imageIndex"
                )
            )
        }
    }

    private fun assets2Drawable(context: Context, fileName: String): Drawable? {
        var open: InputStream? = null
        var drawable: Drawable? = null
        try {
            open = fileName.let { context.assets.open(it) }
            drawable = Drawable.createFromStream(open, null)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                open?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return drawable
    }


    companion object {
    }
}