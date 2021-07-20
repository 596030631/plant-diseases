package com.sj.plant.ui.knowledge

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.sj.plant.databinding.FragmentKnowledgeBinding
import com.sj.plant.databinding.ItemKnowledgeBinding
import com.sj.plant.util.Adapter

class KnowledgeFragment : Fragment() {

    private val viewModel: KnowledgeViewModel by lazy {
        ViewModelProvider(requireActivity()).get(KnowledgeViewModel::class.java)
    }
    private lateinit var adapter: Adapter<KnowledgeViewModel.Companion.ContentData, ItemKnowledgeBinding>
    private lateinit var binding: FragmentKnowledgeBinding
    private val listKnowledge: MutableList<KnowledgeViewModel.Companion.ContentData> by lazy {
        viewModel.listKnowledge
    }

    private val imm: InputMethodManager by lazy {
        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentKnowledgeBinding.inflate(inflater, container, false).apply {
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

        viewModel.analysisData.observe(viewLifecycleOwner) {
            binding.editSearch.setText(it.result[1])
        }

        binding.root.setOnClickListener {
            imm.hideSoftInputFromWindow(binding.editSearch.windowToken, 0)
        }
        binding.btnSelect.setOnClickListener {
            imm.hideSoftInputFromWindow(binding.editSearch.windowToken, 0)
            viewModel.searchByName(binding.editSearch.editableText.toString())
            adapter.notifyDataSetChanged()
        }
        adapter = Adapter(listKnowledge, { v, p ->
            v.title.text = listKnowledge[p].title
            v.content.text = listKnowledge[p].detail

            v.root.setOnClickListener {
                viewModel.knowledgeSelect = listKnowledge[p]
                Navigation.findNavController(it)
                    .navigate(KnowledgeFragmentDirections.actionNavigationKnowledgeToDetailFragment())
            }

        }, { p ->
            ItemKnowledgeBinding.inflate(LayoutInflater.from(p.context), p, false)
        })
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.search()
    }
}