package com.sj.canvas.ui.chat

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import java.io.IOException
import java.io.InputStream

class ChatViewModel : ViewModel() {

    val listChat = mutableListOf<Content>()

    init {
        listChat.add(Content(text = "我是您的专属人工助手"))
        listChat.add(Content(text = "您可以对我说:海绵宝宝手绘"))
    }

    data class Content(var left: Boolean = true, var image: Drawable? = null, var text: String = "")


    fun messageResponse(context: Context, input: String): Content? {
        if (input.contains("海绵宝宝")) {
            return Content(image = assets2Drawable(context, "image/haimianbaobao.png"))
        }

        return null
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