package com.sj.canvas.ui.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sj.canvas.R
import com.sj.canvas.ai.AnalysisData
import com.sj.canvas.ai.LoadModelTask

class ChatViewModel : ViewModel() {

    fun search() {

    }

    fun searchByName(name: String) {

    }

    val analysisData: MutableLiveData<AnalysisData> = MutableLiveData()
    val listKnowledge = mutableListOf<ContentData>()
    var knowledgeSelect: ContentData? = null

    companion object {
        data class ContentData(
            var category: Int,
            var title: String,
            internal var detail: String,
            internal var contentText: String,
            val image: Int
        )
    }
}