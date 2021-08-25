package com.sj.canvas.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.sj.canvas.ai.ImageDetectionFloat
import com.sj.canvas.databinding.FragmentChatBinding
import com.sj.canvas.databinding.ItemChatBinding
import com.sj.canvas.util.Adapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.random.Random

class ChatFragment : Fragment() {

    private val viewModel: ChatViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
    }
    private lateinit var adapter: Adapter<ChatViewModel.Content, ItemChatBinding>
    private lateinit var binding: FragmentChatBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentChatBinding.inflate(inflater, container, false).apply {
        binding = this
    }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            val decorView = requireActivity().window.decorView
            val uiOptions = decorView.systemUiVisibility
            var newUiOptions = uiOptions
            newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE
            newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_FULLSCREEN
            newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            decorView.systemUiVisibility = newUiOptions
        }

        binding.btnBack.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }


        adapter = Adapter(viewModel.listChat, { v, p ->
            val content: ChatViewModel.Content = viewModel.listChat[p]
            if (content.left) {
                v.layoutRight.visibility = View.GONE
                v.layoutLeft.visibility = View.VISIBLE
                if (content.image != null) {
                    Log.d("et_log", "设置图片信息")
                    v.imageLeft.setImageDrawable(content.image)
                    v.layoutTextLeft.visibility = View.GONE
                    v.layoutImageLeft.visibility = View.VISIBLE
                    v.btnAnalysis.setOnClickListener {
                        viewModel.listChat.add(ChatViewModel.Content(true, null, "让我来揭晓答案吧"))
                        adapter.notifyDataSetChanged()
                        binding.recyclerview.scrollToPosition(viewModel.listChat.size - 1)

                        if (ImageDetectionFloat.getInstance().available()) {
                            Single.just(content.image?.toBitmap())
                                .map {
                                    if (it == null) return@map arrayOf("我也不会了，这确实很难，可以让我换一个")
                                    return@map ImageDetectionFloat.getInstance().detection(it)
                                }
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.single())
                                .subscribe({
                                    v.btnAnalysis.pauseAnimation()
                                    if (it != null && it.isNotEmpty()) {
                                        it.forEach { item ->
                                            viewModel.listChat.add(
                                                ChatViewModel.Content(
                                                    true,
                                                    null,
                                                    item ?: "我不会"
                                                )
                                            )
                                        }
                                    }
                                    adapter.notifyDataSetChanged()
                                    binding.recyclerview.scrollToPosition(viewModel.listChat.size - 1)
                                }, {
                                    it.printStackTrace()
                                })
                        }
                    }
                } else {
                    Log.d("et_log", "设置文本信息")
                    v.textLeft.text = content.text
                    v.layoutImageLeft.visibility = View.GONE
                    v.layoutTextLeft.visibility = View.VISIBLE
                }
            } else {
                v.layoutLeft.visibility = View.GONE
                v.layoutRight.visibility = View.VISIBLE
                v.textRight.text = content.text
            }
        }, { p ->
            ItemChatBinding.inflate(LayoutInflater.from(p.context), p, false)
        })
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.VERTICAL
        }

        binding.inputMessage.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                binding.inputMessage.text.toString().also {
                    if (it.isEmpty()) return@also
                    viewModel.listChat.add(
                        ChatViewModel.Content(
                            false,
                            null,
                            it
                        )
                    )
                    adapter.notifyItemInserted(viewModel.listChat.size - 1)
                    binding.inputMessage.setText("")
                    binding.recyclerview.scrollToPosition(viewModel.listChat.size - 1)

                    viewModel.messageResponse(requireContext(), it).let {
                        binding.recyclerview.postDelayed({
                            adapter.notifyDataSetChanged()
                            binding.recyclerview.scrollToPosition(viewModel.listChat.size - 1)
                        }, Random.nextLong(2_000) + 500)
                    }
                }
            }
            true
        }
    }

}