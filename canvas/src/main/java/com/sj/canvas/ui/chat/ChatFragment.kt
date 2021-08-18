package com.sj.canvas.ui.chat

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.keyboard.view.R
import com.sj.canvas.databinding.FragmentChatBinding
import com.sj.canvas.databinding.ItemKnowledgeBinding
import com.sj.canvas.util.Adapter
import com.sj.emoji.DefEmoticons
import com.sj.emoji.EmojiBean
import com.sj.emoji.EmojiDisplay
import com.sj.emoji.EmojiSpan
import sj.keyboard.adpater.EmoticonsAdapter
import sj.keyboard.adpater.PageSetAdapter
import sj.keyboard.data.EmoticonPageEntity
import sj.keyboard.data.EmoticonPageSetEntity
import sj.keyboard.interfaces.EmoticonClickListener
import sj.keyboard.interfaces.EmoticonDisplayListener
import sj.keyboard.interfaces.EmoticonFilter
import sj.keyboard.interfaces.PageViewInstantiateListener
import sj.keyboard.utils.EmoticonsKeyboardUtils
import sj.keyboard.widget.EmoticonPageView
import java.util.*
import java.util.regex.Matcher

class ChatFragment : Fragment() {

    private val viewModel: ChatViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
    }
    private lateinit var adapter: Adapter<ChatViewModel.Companion.ContentData, ItemKnowledgeBinding>
    private lateinit var binding: FragmentChatBinding
    private val listKnowledge: MutableList<ChatViewModel.Companion.ContentData> by lazy {
        viewModel.listKnowledge
    }

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

        adapter = Adapter(listKnowledge, { v, p ->
            v.title.text = listKnowledge[p].title
            v.content.text = listKnowledge[p].detail

            v.root.setOnClickListener {
                viewModel.knowledgeSelect = listKnowledge[p]
                Navigation.findNavController(it)
                    .navigate(ChatFragmentDirections.actionNavigationKnowledgeToDetailFragment())
            }

        }, { p ->
            ItemKnowledgeBinding.inflate(LayoutInflater.from(p.context), p, false)
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
//        binding.editSearch.setOnEditorActionListener { v, actionId, event ->
//            if (actionId == EditorInfo.IME_ACTION_SEND) {
//                binding.editSearch.setText("")
//            }
//            true
//        }


        // source data

        // source data
        val emojiArray: ArrayList<EmojiBean> = ArrayList<EmojiBean>()
        Collections.addAll(emojiArray, *DefEmoticons.getDefEmojiArray())

        // emoticon click

        // emoticon click
        val emoticonClickListener: EmoticonClickListener<*> =
            EmoticonClickListener<Any?> { o, actionType, isDelBtn ->
                if (isDelBtn) {
                    val action = KeyEvent.ACTION_DOWN
                    val code = KeyEvent.KEYCODE_DEL
                    val event = KeyEvent(action, code)
                    binding.ekBar.etChat.onKeyDown(KeyEvent.KEYCODE_DEL, event)
                } else {
                    if (o == null) {
                        return@EmoticonClickListener
                    }
                    var content: String? = null
                    if (o is EmojiBean) {
                        content = (o as EmojiBean).emoji
                    }
                    val index = binding.ekBar.etChat.selectionStart
                    val editable = binding.ekBar.etChat.text
                    editable.insert(index, content)
                }
            }

        // emoticon instantiate

        // emoticon instantiate
        val emoticonDisplayListener: EmoticonDisplayListener<*> =
            EmoticonDisplayListener<Any?> { i, viewGroup, viewHolder, `object`, isDelBtn ->
                val emojiBean: EmojiBean = `object` as EmojiBean
                if (emojiBean == null && !isDelBtn) {
                    return@EmoticonDisplayListener
                }
                viewHolder.ly_root.setBackgroundResource(R.drawable.bg_emoticon)
                if (isDelBtn) {
                    viewHolder.iv_emoticon.setImageResource(R.drawable.btn_voice)
                } else {
                    viewHolder.iv_emoticon.setImageResource(emojiBean.icon)
                }
                viewHolder.rootView.setOnClickListener {
                    emoticonClickListener.onEmoticonClick(
                        emojiBean as Nothing?,
                        0,
                        isDelBtn
                    )
                }
            }

        //  page instantiate

        //  page instantiate
        val pageViewInstantiateListener: PageViewInstantiateListener<*> =
            PageViewInstantiateListener<EmoticonPageEntity<*>> { viewGroup, i, pageEntity ->
                if (pageEntity.rootView == null) {
                    val pageView = EmoticonPageView(viewGroup.context)
                    pageView.setNumColumns(pageEntity.row)
                    pageEntity.rootView = pageView
                    try {
                        val adapter: EmoticonsAdapter<*> =
                            EmoticonsAdapter<Any?>(viewGroup.context, pageEntity, null)
                        // emoticon instantiate
                        adapter.setOnDisPlayListener(emoticonDisplayListener)
                        pageView.emoticonsGridView.adapter = adapter
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                pageEntity.rootView
            }

        // build

        // build
        val xhsPageSetEntity: EmoticonPageSetEntity<*> = EmoticonPageSetEntity.Builder<Any?>()
            .setLine(3)
            .setRow(7)
            .setEmoticonList(emojiArray)
            .setIPageViewInstantiateItem(pageViewInstantiateListener)
            .setShowDelBtn(EmoticonPageEntity.DelBtnStatus.LAST)
            .setIconUri(R.drawable.icon_add_nomal)
            .build()

        val pageSetAdapter = PageSetAdapter()
        pageSetAdapter.add(xhsPageSetEntity)
        binding.ekBar.setAdapter(pageSetAdapter)


        class EmojiFilter : EmoticonFilter() {
            private var emojiSize = -1
            override fun filter(
                editText: EditText,
                text: CharSequence,
                start: Int,
                lengthBefore: Int,
                lengthAfter: Int
            ) {
                emojiSize =
                    if (emojiSize == -1) EmoticonsKeyboardUtils.getFontHeight(editText) else emojiSize
                clearSpan(editText.text, start, text.toString().length)
                val m: Matcher = EmojiDisplay.getMatcher(
                    text.toString().substring(start, text.toString().length)
                )
                if (m != null) {
                    while (m.find()) {
                        val emojiHex = Integer.toHexString(Character.codePointAt(m.group(), 0))
                        val drawable: Drawable = getDrawable(
                            editText.context,
                            EmojiDisplay.HEAD_NAME.toString() + emojiHex
                        )
                        if (drawable != null) {
                            var itemHeight: Int
                            var itemWidth: Int
                            if (emojiSize == EmojiDisplay.WRAP_DRAWABLE) {
                                itemHeight = drawable.intrinsicHeight
                                itemWidth = drawable.intrinsicWidth
                            } else {
                                itemHeight = emojiSize
                                itemWidth = emojiSize
                            }
                            drawable.setBounds(0, 0, itemHeight, itemWidth)
                            val imageSpan = EmojiSpan(drawable)
                            editText.text.setSpan(
                                imageSpan,
                                start + m.start(),
                                start + m.end(),
                                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                }
            }

            private fun clearSpan(spannable: Spannable, start: Int, end: Int) {
                if (start == end) {
                    return
                }
                val oldSpans: Array<EmojiSpan> =
                    spannable.getSpans(start, end, EmojiSpan::class.java)
                for (i in oldSpans.indices) {
                    spannable.removeSpan(oldSpans[i])
                }
            }
        }
        // add a filter
        // add a filter
        binding.ekBar.etChat.addEmoticonFilter(EmojiFilter())




    }

    override fun onResume() {
        super.onResume()
        viewModel.search()
    }
}