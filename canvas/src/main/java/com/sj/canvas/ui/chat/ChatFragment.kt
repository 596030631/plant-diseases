package com.sj.canvas.ui.chat

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
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

    private var marginSetting = false

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

        binding.inputMessage.setOnClickListener {
            showKeyBoard(true)
        }

        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            //获取当前界面可视部分
            requireActivity().window.decorView.getWindowVisibleDisplayFrame(r)
            //获取屏幕的高度
            val screenHeight: Int =
                requireActivity().window.decorView.rootView.height
            //此处就是用来获取键盘的高度的， 在键盘没有弹出的时候 此高度为0 键盘弹出的时候为一个正数
            val heightDifference: Int = screenHeight - r.bottom
            Log.d("TAG", "Size: $heightDifference")
            if (heightDifference < 20) {
                showKeyBoard(false)
            } else {
                showKeyBoard(true)
            }
        }

//        viewModel.analysisData.observe(viewLifecycleOwner) {
//            binding.editSearch.setText(it.label)
//        }

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
                v.layoutReceive.visibility = View.VISIBLE
                v.layoutSend.visibility = View.GONE
                v.textReceive.text = content.text
            } else {
                v.layoutReceive.visibility = View.GONE
                v.layoutSend.visibility = View.VISIBLE
                v.textSender.text = content.text
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
                viewModel.listChat.add(
                    ChatViewModel.Content(
                        false,
                        binding.inputMessage.text.toString()
                    )
                )
                adapter.notifyItemInserted(viewModel.listChat.size - 1)
                binding.inputMessage.setText("")
//                binding.recyclerview.scrollToPosition(viewModel.listChat.size - 1)
            }
            true
        }

    }


    override fun onResume() {
        super.onResume()
        viewModel.search()
    }

    private fun showKeyBoard(boolean: Boolean) {
        if (marginSetting == boolean) return
        if (boolean) {
            marginSetting = true
            binding.layoutInput.postDelayed({
                val param: LinearLayout.LayoutParams =
                    binding.layoutInput.layoutParams as LinearLayout.LayoutParams
                param.bottomMargin = 16
                binding.layoutInput.layoutParams = param
            }, 100)
        } else {
            marginSetting = false
            binding.layoutInput.post {
                val param: LinearLayout.LayoutParams =
                    binding.layoutInput.layoutParams as LinearLayout.LayoutParams
                param.bottomMargin = 0
                binding.layoutInput.layoutParams = param
            }
        }
    }
}