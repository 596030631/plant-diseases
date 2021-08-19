package com.sj.canvas.ui.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.sj.canvas.databinding.FragmentChatBinding
import com.sj.canvas.databinding.ItemChatBinding
import com.sj.canvas.util.Adapter

class ChatFragment : Fragment() {

    private val viewModel: ChatViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
    }
    private lateinit var adapter: Adapter<ChatViewModel.Content, ItemChatBinding>
    private lateinit var binding: FragmentChatBinding

    private val imm: InputMethodManager by lazy {
        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

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

//        viewModel.analysisData.observe(viewLifecycleOwner) {
//            binding.editSearch.setText(it.label)
//        }
//
//        binding.root.setOnClickListener {
//            imm.hideSoftInputFromWindow(binding.editSearch.windowToken, 0)
//        }

//        binding.recyclerview.setOnClickListener {
//            imm.hideSoftInputFromWindow(binding.editSearch.windowToken, 0)
//        }

        var content: ChatViewModel.Content
        adapter = Adapter(viewModel.listChat, { v, p ->
//            v.title.text = listKnowledge[p].title
//            v.content.text = listKnowledge[p].detail
            content = viewModel.listChat[p]
            if (content.left) {
                v.itemWechatMsgTvReceiverMsg.text = content.text
            } else {
                v.itemWechatMsgTvSenderMsg.text = content.text
            }

            v.root.setOnClickListener {
                Navigation.findNavController(it)
                    .navigate(ChatFragmentDirections.actionNavigationKnowledgeToDetailFragment())
            }

        }, { p ->
            ItemChatBinding.inflate(LayoutInflater.from(p.context), p, false)
        })
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.VERTICAL
        }


//        binding.editSearch.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                s?.let {
//                    viewModel.searchByName(it.toString())
//                    adapter.notifyDataSetChanged()
//                }
//            }
//
//        })
//
        binding.activityWechatChatEtMsg.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                viewModel.listChat.add(
                    ChatViewModel.Content(
                        false,
                        binding.activityWechatChatEtMsg.text.toString()
                    )
                )
                adapter.notifyItemInserted(viewModel.listChat.size - 1)
                binding.activityWechatChatEtMsg.setText("")
            }
            true
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.search()
    }
}