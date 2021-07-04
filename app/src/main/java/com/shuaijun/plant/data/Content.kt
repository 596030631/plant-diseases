package com.shuaijun.plant.data

import android.os.Environment
import java.io.File

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
        fun loadFromFile(): MutableList<Content> {
            val fileRoot =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val advert = File(fileRoot, "advert")
            if (advert.exists()) {
                val list = advert.listFiles() ?: return mutableListOf()
                if (list.isNotEmpty()) {
                    val contents = mutableListOf<Content>()
                    list.forEach {
                        if (it.exists() && it.isFile && it.canRead() && it.endsWith(".txt")) {
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