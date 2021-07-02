package com.shuaijun.plant.ui

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface.ROTATION_0
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.orhanobut.logger.Logger
import com.shuaijun.plant.R
import com.shuaijun.plant.databinding.CameraFragmentBinding
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class CameraFragment : BaseFragment() {

    companion object {

        private const val TAG = "CameraFragment"
        private const val IMAGE_PICK = 1

        @JvmStatic
        fun newInstance() = CameraFragment()
    }

    private lateinit var viewModel: CameraViewModel
    private lateinit var binding: CameraFragmentBinding
    private lateinit var cameraExecutor: Executor
    private lateinit var imageCapture: ImageCapture

    private var processCode = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CameraFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CameraViewModel::class.java)
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnClose.setOnClickListener {
            mainModel.fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment.newInstance()).commit()
        }

        binding.btnCapture.setOnClickListener {
            processCode = 1

//            val file = File("/sdcard/DCIM/${System.currentTimeMillis()}.jpg")
//            val op = ImageCapture.OutputFileOptions.Builder(file).build()
//            imageCapture.takePicture(op, cameraExecutor,
//                object : ImageCapture.OnImageSavedCallback {
//                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                        Logger.d("拍照成功")
//                        mainModel.analysisImage.postValue(file.absolutePath)
//                        sava2gallery(requireContext().contentResolver, file)
//                    }
//
//                    override fun onError(exception: ImageCaptureException) {
//                        Logger.d("拍照失败")
//                    }
//                })
        }

        binding.btnPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK)
        }
    }

    private fun sava2gallery(cr: ContentResolver, file: File): Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "player")
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "player")
        values.put(MediaStore.Images.Media.DESCRIPTION, "test")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DATA, file.absolutePath)
        return cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    override fun onResume() {
        super.onResume()
        startCamera()
        processCode = 0
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.preview.surfaceProvider)
                }
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setTargetRotation(ROTATION_0)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                .build()
            imageCapture = ImageCapture.Builder().build()
            imageAnalysis.setAnalyzer(cameraExecutor, { image ->
                when (processCode) {
                    0 -> image.close()
                    1 -> {
                        requireActivity().runOnUiThread {
                            cameraProvider.unbindAll()
                        }
                        processCode = 2
                        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        root.mkdir()
                        val file = File("/sdcard/DCIM/${System.currentTimeMillis()}.jpg")
                        file.createNewFile()
                        val bmp = image.toBitmap()
                        bmp?.compress(
                            Bitmap.CompressFormat.JPEG,
                            100,
                            file.outputStream()
                        ); // bmp is your Bitmap instance
                        mainModel.analysisImage.postValue(file.absolutePath)
                        sava2gallery(requireContext().contentResolver, file)

//                        val file = File("/sdcard/DCIM/${System.currentTimeMillis()}.jpg")
//                        val op = ImageCapture.OutputFileOptions.Builder(file).build()
//                        imageCapture.takePicture(op, cameraExecutor,
//                            object : ImageCapture.OnImageSavedCallback {
//                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                                    Logger.d("拍照成功")
//                                    mainModel.analysisImage.postValue(file.absolutePath)
//                                    sava2gallery(requireContext().contentResolver, file)
//                                }
//
//                                override fun onError(exception: ImageCaptureException) {
//                                    Logger.d("拍照失败")
//                                }
//                            })
                    }
                    2 -> {
                        Logger.d(image.imageInfo)
                    }
                }
            })
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    requireActivity(), cameraSelector, imageAnalysis, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun ImageProxy.toBitmap(): Bitmap? {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        Logger.d(bytes.size)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Logger.d(data)
        if (requestCode == IMAGE_PICK) {
            Logger.d(data?.data)
            data?.let {
                it.data?.let { uri ->
                    val path = getFilePathFromContentUri(uri, requireContext().contentResolver)
                    Logger.d(path)
                    val file = File(path)
                }
            }
        }
    }

    /**
     * Gets the corresponding path to a file from the given content:// URI
     * @param selectedVideoUri The content:// URI to find the file path from
     * @param contentResolver The content resolver to use to perform the query.
     * @return the file path as a string
     */
    private fun getFilePathFromContentUri(
        selectedVideoUri: Uri,
        contentResolver: ContentResolver
    ): String? {
        val filePath: String
        val filePathColumn = arrayOf(MediaColumns.DATA)
        val cursor: Cursor =
            contentResolver.query(selectedVideoUri, filePathColumn, null, null, null) ?: return null
        cursor.moveToFirst()
        val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
        filePath = cursor.getString(columnIndex)
        cursor.close()
        return filePath
    }
}