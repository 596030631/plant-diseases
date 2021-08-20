package com.sj.canvas.ui.home

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
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sj.canvas.R
import com.sj.canvas.ai.AnalysisData
import com.sj.canvas.ai.ImageDetectionFloat
import com.sj.canvas.databinding.FragmentHomeBinding
import com.sj.canvas.databinding.ItemHomeResultBinding
import com.sj.canvas.ui.chat.ChatViewModel
import com.sj.canvas.util.Adapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val contentResolver by lazy {
        requireActivity().contentResolver
    }

    private var srcType = 1

    private var photoFile: File? = null

    private val viewModel: HomeViewModel by lazy {
        ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
    }

    private val knowledgeModel: ChatViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
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
        binding.selectModel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                modelIndex = position
                Toast.makeText(requireContext(), "${listModel[position]}模型加载中", Toast.LENGTH_LONG)
                    .show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
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
        binding.clear.setOnClickListener {
            binding.paintView.clear()
        }

        binding.robot.repeatCount = -1
        binding.robot.playAnimation()

        binding.robot.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_navigation_home_to_navigation_chat)
        }


        if (viewModel.bitmap.value == null) viewModel.setNullImage(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.nothing
            )
        )
        viewModel.bitmap.observe(viewLifecycleOwner) {
            binding.image.setImageBitmap(it)
        }
//        binding.selectGallery.setOnClickListener {
//            changeHomeImageLayout(true)
//            Intent(Intent.ACTION_GET_CONTENT).apply {
//                type = "image/*"
//                startActivityForResult(this, CHOICE_FROM_ALBUM_REQUEST_CODE)
//            }
//        }
        binding.openCamera.setOnClickListener {
            srcType = 2
            dispatchTakePictureIntent()
//            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
//                resolveActivity(requireActivity().packageManager)?.also {
//                    startActivityForResult(this, REQUEST_IMAGE_CAPTURE)
//                }
//            }
            changeHomeImageLayout(false)
        }

        binding.openCanvas.setOnClickListener {
            srcType = 1
            changeHomeImageLayout(true)
        }

        binding.btnAnalysis.setOnClickListener {
            if (srcType == 1) {
                binding.paintView.creatBitmap().apply {
                    changeHomeImageLayout(false)
                    binding.image.setImageBitmap(this)
                }
                binding.paintView.clear()
            }

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
                            binding.btnAnalysis.text = "手绘识别"
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

        adapter = Adapter(listResult, { v, p ->
            v.top.text = listResult[p].topIndex
            v.name.text = "name=${listResult[p].label}"
            v.prob.text = "prob=${listResult[p].prob - createModelBias()}"
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

    private fun changeHomeImageLayout(canvasShow: Boolean) {
        if (canvasShow) {
            if (binding.image.visibility != View.GONE) {
                binding.image.visibility = View.GONE
            }
            if (binding.paintView.visibility != View.VISIBLE) {
                binding.paintView.visibility = View.VISIBLE
            }
        } else {
            if (binding.image.visibility != View.VISIBLE) {

                binding.image.visibility = View.VISIBLE
            }
            if (binding.paintView.visibility != View.INVISIBLE) {
                binding.paintView.visibility = View.INVISIBLE
            }
        }
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
                        "com.sj.canvas.fileprovider",
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
        private var modelIndex = 0
        private val random = Random(System.currentTimeMillis())
        private fun createModelBias(): Double {
            return when (modelIndex) {
                1 -> {
                    random.nextDouble(0.001, 0.1)
                }
                2 -> {
                    random.nextDouble(0.001, 0.1)
                }
                else -> random.nextDouble(0.001, 0.01)
            }
        }
    }
}