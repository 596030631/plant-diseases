package com.sj.canvas.ui.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sj.canvas.ai.AnalysisData

class ChatViewModel : ViewModel() {

    val listChat = mutableListOf<Content>()

    init {
        listChat.add(Content(text = "我是你爸爸"))
    }

    fun search() {

    }

    fun searchByName(name: String) {

    }

    val analysisData: MutableLiveData<AnalysisData> = MutableLiveData()

    data class Content(var left: Boolean = true, var text: String = "")



    companion object {
    }
}