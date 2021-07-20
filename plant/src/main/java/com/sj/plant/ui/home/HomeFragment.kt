package com.sj.plant.ui.home

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sj.plant.R
import com.sj.plant.ai.AnalysisData
import com.sj.plant.ai.ImageDetectionFloat
import com.sj.plant.databinding.FragmentHomeBinding
import com.sj.plant.databinding.ItemHomeResultBinding
import com.sj.plant.ui.knowledge.KnowledgeViewModel
import com.sj.plant.util.Adapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val contentResolver by lazy {
        requireActivity().contentResolver
    }

    private var photoFile: File? = null

    private val viewModel: HomeViewModel by lazy {
        ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
    }

    private val knowledgeModel: KnowledgeViewModel by lazy {
        ViewModelProvider(requireActivity()).get(KnowledgeViewModel::class.java)
    }

    private val listResult = mutableListOf<AnalysisData>()
    private lateinit var adapter: Adapter<AnalysisData, ItemHomeResultBinding>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentHomeBinding.inflate(inflater, container, false).apply {
        binding = this
    }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.selectModel.adapter = ArrayAdapter(
            requireActivity(), android.R.layout.simple_list_item_1, listModel
        )
        requireActivity().window.decorView.setOnSystemUiVisibilityChangeListener {
            val decorView = requireActivity().window.decorView
            val uiOptions = decorView.systemUiVisibility
            var newUiOptions = uiOptions
            newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE
            newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_FULLSCREEN
            newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            decorView.systemUiVisibility = newUiOptions
        }

        initEvent()
    }

    private fun initEvent() {
        if (viewModel.bitmap.value == null) viewModel.setNullImage(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.nothing
            )
        )
        viewModel.bitmap.observe(viewLifecycleOwner) {
            binding.image.setImageBitmap(it)
        }
        binding.selectGallery.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                startActivityForResult(this, CHOICE_FROM_ALBUM_REQUEST_CODE)
            }
        }
        binding.openCamera.setOnClickListener {
            dispatchTakePictureIntent()
//            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
//                resolveActivity(requireActivity().packageManager)?.also {
//                    startActivityForResult(this, REQUEST_IMAGE_CAPTURE)
//                }
//            }
        }
        binding.btnAnalysis.setOnClickListener {
            if (ImageDetectionFloat.getInstance().available()) {
                if (viewModel.bitmap.value == null) {
                    Toast.makeText(requireContext(), "请先选择图片", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                binding.btnAnalysis.text = "正在识别"
                binding.btnAnalysis.isEnabled = false
                viewModel.bitmap.value?.let { it1 ->
                    Single.just(it1).map {
                        ImageDetectionFloat.getInstance().detection(it)
                    }
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            binding.btnAnalysis.text = "虫害识别"
                            binding.btnAnalysis.isEnabled = true
                            if (it.isNotEmpty()) {
                                listResult.clear()
                                it.forEach { top ->
                                    listResult.add(top)
                                }
                                adapter.notifyDataSetChanged()
                            }
                        }, {
                            binding.btnAnalysis.text = "虫害识别"
                            binding.btnAnalysis.isEnabled = true
                            Toast.makeText(requireContext(), "识别异常", Toast.LENGTH_SHORT).show()
                        })
                }
            }
        }

        binding.btnKnowledge.setOnClickListener { view ->
            if (listResult.isEmpty()) {
                Toast.makeText(requireContext(), "请识别后选择查看", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            listResult.forEach {
                if (it.select) {
                    knowledgeModel.analysisData.value = it
                    Navigation.findNavController(view)
                        .navigate(HomeFragmentDirections.actionNavigationHomeToNavigationDashboard())
                    return@setOnClickListener
                }
            }
        }

        adapter = Adapter(listResult, { v, p ->
            v.top.text = listResult[p].result[0]
            v.name.text = "name=${listResult[p].result[1]}"
            v.prob.text = listResult[p].result[2]
            v.root.setBackgroundColor(
                if (listResult[p].select) Color.rgb(
                    0, 0x91, 0xea
                ) else Color.rgb(
                    0xe2,
                    0xe3,
                    0xe4
                )
            )
            v.root.setOnClickListener {
                for ((i, anal) in listResult.withIndex()) {
                    anal.select = i == p
                }
                adapter.notifyDataSetChanged()
            }
        }, { p -> ItemHomeResultBinding.inflate(LayoutInflater.from(p.context), p, false) })
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext()).also {
            it.orientation = LinearLayoutManager.VERTICAL
        }
        binding.recyclerview.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                photoFile = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.sj.plant.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("et_log", "onActivityResult $requestCode  $data")
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                photoFile?.let { it ->
                    Single.just(it)
                        .map {
                            val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                            Bitmap.createScaledBitmap(
                                bitmap,
                                binding.image.width,
                                binding.image.height,
                                true
                            )
                        }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            binding.image.scaleType = ImageView.ScaleType.FIT_XY
                            viewModel.bitmap.postValue(it)
                        }, {
                            Toast.makeText(
                                requireContext(),
                                it.message ?: return@subscribe,
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                }

                val uri = data?.data
                uri?.apply {
                    Log.e("et_log", "uri=$this")
                    Single.just(this)
                        .map {
                            contentResolver.openInputStream(this).use {
                                val bitmap = BitmapFactory.decodeStream(it)
                                Bitmap.createScaledBitmap(
                                    bitmap,
                                    binding.image.width,
                                    binding.image.height,
                                    true
                                )
                            }
                        }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            binding.image.scaleType = ImageView.ScaleType.FIT_XY
                            viewModel.bitmap.postValue(it)
                        }, {
                            Toast.makeText(
                                requireContext(),
                                it.message ?: return@subscribe,
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                }

            } else if (requestCode == CHOICE_FROM_ALBUM_REQUEST_CODE) {
                val uri = data?.data
                uri?.apply {
                    Log.e("et_log", "uri=$this")
                    Single.just(this)
                        .map {
                            contentResolver.openInputStream(this).use {
                                val bitmap = BitmapFactory.decodeStream(it)
                                Bitmap.createScaledBitmap(
                                    bitmap,
                                    binding.image.width,
                                    binding.image.height,
                                    true
                                )
                            }
                        }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            binding.image.scaleType = ImageView.ScaleType.FIT_XY
                            viewModel.bitmap.postValue(it)
                        }, {
                            Toast.makeText(
                                requireContext(),
                                it.message ?: return@subscribe,
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                }
            }
        } else {
            Toast.makeText(requireContext(), "操作失败", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val CHOICE_FROM_ALBUM_REQUEST_CODE = 1
        const val REQUEST_IMAGE_CAPTURE = 2
        private val listModel = arrayListOf("浮点模型", "量化模型", "其他模型")
    }
}