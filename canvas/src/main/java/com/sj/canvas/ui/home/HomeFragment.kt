package com.sj.canvas.ui.home

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
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
import com.sj.canvas.R
import com.sj.canvas.ai.ImageDetectionFloat
import com.sj.canvas.databinding.FragmentHomeBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val contentResolver by lazy {
        requireActivity().contentResolver
    }

    private var photoFile: File? = null

    private val viewModel: HomeViewModel by lazy {
        ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
    }

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

        binding.loading.setLoadingColor(R.color.bg)

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
        binding.btnClear.setOnClickListener {
            binding.paintView.clear()
        }

        binding.robot.repeatCount = -1
        binding.robot.playAnimation()

        binding.robot.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_navigation_home_to_navigation_chat)
        }

        binding.btnSave.setOnClickListener {
            showAnalysisButton(true)
            binding.paintView.creatBitmap().apply {
                viewModel.bitmap.value = this
                showImage()
                binding.image.setImageBitmap(this)
                createImageFile().also {
                    try {
                        val out = FileOutputStream(it)
                        this.compress(Bitmap.CompressFormat.JPEG, 80, out)
                        out.flush()
                        out.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            binding.paintView.clear()

        }

        binding.openCamera.setOnClickListener {
            showImage()
            showAnalysisButton(false)
            dispatchTakePictureIntent()
        }

        binding.openCanvas.setOnClickListener {
            showCanvas()
        }

        binding.btnAnalysis.setOnClickListener {
            hideAnalysisButton()
            if (ImageDetectionFloat.getInstance().available()) {
                if (viewModel.bitmap.value == null) {
                    Toast.makeText(requireContext(), "请先选择图片", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                binding.loading.visibility = View.VISIBLE
                binding.loading.start()
                viewModel.bitmap.value?.let { it1 ->
                    Single.just(it1).map {
                        ImageDetectionFloat.getInstance().detection(it)
                    }
                        .subscribeOn(Schedulers.single())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            viewModel.bitmap.value = null
                            binding.loading.visibility = View.GONE
                            binding.loading.stop()
                            if (it.isNotEmpty()) {
                                showResult(it)
                            }
                        }, {
                            it.printStackTrace()
                            binding.loading.visibility = View.GONE
                            binding.loading.stop()
                            Toast.makeText(requireContext(), "识别异常", Toast.LENGTH_SHORT).show()
                        })
                }
            }
        }
    }

    private fun showCanvas() {
        if (binding.image.visibility != View.INVISIBLE) {
            binding.image.visibility = View.INVISIBLE
        }
        if (binding.layoutBoard.visibility != View.VISIBLE) {
            binding.layoutBoard.visibility = View.VISIBLE
        }
    }

    private fun showImage() {
        if (binding.image.visibility != View.VISIBLE) {
            binding.image.visibility = View.VISIBLE
        }
        if (binding.layoutBoard.visibility != View.INVISIBLE) {
            binding.layoutBoard.visibility = View.INVISIBLE
        }
    }

    private fun showAnalysisButton(black: Boolean) {
        binding.btnAnalysis.visibility = View.VISIBLE
        if (black) {
            binding.iconAirbnb.setImageDrawable(requireActivity().resources.getDrawable(R.drawable.airbnb_black))
            binding.labelAnal.setTextColor(Color.BLACK)
        } else {
            binding.iconAirbnb.setImageDrawable(requireActivity().resources.getDrawable(R.drawable.airbnb))
            binding.labelAnal.setTextColor(Color.WHITE)
        }
    }

    private fun hideAnalysisButton() {
        binding.btnAnalysis.visibility = View.GONE
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
                            var degree = 0
                            val exifInterface = ExifInterface(it.absolutePath)
                            val orientation = exifInterface.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL
                            )
                            when (orientation) {
                                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                            }

                            val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                            val matrix = Matrix()
                            matrix.postRotate(degree.toFloat())
                            Log.e("et_log", "degree:$degree")
                            Bitmap.createBitmap(
                                bitmap,
                                0, 0,
                                bitmap.width,
                                bitmap.height,
                                matrix,
                                true
                            ).apply {
                                return@map Bitmap.createScaledBitmap(
                                    this,
                                    193,
                                    256,
                                    true
                                )
                            }
                        }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            viewModel.bitmap.value = (it)
                            binding.image.scaleType = ImageView.ScaleType.FIT_XY
                            binding.image.setImageBitmap(it)
                        }, {
                            Toast.makeText(
                                requireContext(),
                                it.message ?: return@subscribe,
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                }

//                val uri = data?.data
//                uri?.apply {
//                    Log.e("et_log", "uri=$this")
//                    Single.just(this)
//                        .map {
//                            contentResolver.openInputStream(this).use {
//                                val bitmap = BitmapFactory.decodeStream(it)
//                                Bitmap.createScaledBitmap(
//                                    bitmap,
//                                    binding.image.width,
//                                    binding.image.height,
//                                    true
//                                )
//                            }
//                        }.subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe({
//                            binding.btnAnalysis.visibility = View.VISIBLE
//                            binding.image.scaleType = ImageView.ScaleType.FIT_XY
//                            viewModel.bitmap.postValue(it)
//                        }, {
//                            Toast.makeText(
//                                requireContext(),
//                                it.message ?: return@subscribe,
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        })
//                }

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

    private fun showResult(list: Array<String?>) {
        AlertDialog.Builder(requireContext())
            .setTitle("识别结果")
            .setItems(list, { dialog, which ->

            })
            .setNegativeButton("确定", DialogInterface.OnClickListener { dialog, which -> })
            .setPositiveButton("取消", DialogInterface.OnClickListener { dialog, which -> })
            .setCancelable(true)
            .create().show()
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