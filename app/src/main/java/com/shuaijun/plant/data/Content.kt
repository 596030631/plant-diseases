package com.shuaijun.plant.data

import android.annotation.SuppressLint
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream

data class Content(
    var id: Int,
    var title: String,
    var category: String,
    var content: String,
    var time: String,
    var star: Int,
    var read: Int
) {
    companion object {
        @SuppressLint("SdCardPath")
        fun loadFromFile(): MutableList<Content> {
            val advert = File("/sdcard")
            if (advert.exists()) {
                val list = advert.listFiles() ?: return mutableListOf()
                if (list.isNotEmpty()) {
                    val contents = mutableListOf<Content>()
                    list.forEach {
                        if (it.exists() && it.isFile && it.canRead() && it.endsWith(".json")) {
                            val sb = StringBuilder()
                            FileInputStream(it).use { f ->
                                while (f.read().also { i -> sb.append(i) } > 0) {
                                }
                            }
                            val jsonObject = JSONObject(sb.toString())
                            Log.e("et_log", jsonObject.getString("title"))
                            Log.e("et_log", jsonObject.getString("content"))
                            Log.e("et_log", jsonObject.getString("star"))
                            val i = Content(1, "Test", "Test", "test", "today", 1, 1)
                            contents.add(i)
                        }
                    }
                }
            } else {
                advert.mkdir()
            }
            return mutableListOf()
        }
    }
}