package com.sj.canvas.ui.chat

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import java.io.IOException
import java.io.InputStream
import kotlin.random.Random

class ChatViewModel : ViewModel() {

    val listChat = mutableListOf<Content>()

    init {
        listChat.add(Content(text = "我是您的专属人工助手"))
        listChat.add(Content(text = "您可以对我说:我要猜手绘"))
    }

    data class Content(var left: Boolean = true, var image: Drawable? = null, var text: String = "")

    private var imageIndex = 0

    fun messageResponse(context: Context, input: String): Content? {
        return when {
            input.contains("我要猜手绘") -> {
                Content(true, null, "准备好了吗，我要开始喽!").also {
                    messageResponse(context, "596030631")
                }
            }
            input.contains("596030631") -> {
                var i: Int
                do {
                    i = Random.nextInt(10)
                    if (imageIndex != i) {
                        break
                    }
                } while (true)
                Content(image = assets2Drawable(context, "image/$imageIndex.png"))
            }
            input.contains("换") -> {
                Content(true, null, "好呀，马上来喽！").apply {
                    messageResponse(context, "596030631")
                }
            }
            else -> Content(true, null, "我还不太理解")
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