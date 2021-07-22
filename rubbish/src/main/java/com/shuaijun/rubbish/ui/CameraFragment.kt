package com.shuaijun.rubbish.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.shuaijun.rubbish.MainViewModel
import com.shuaijun.rubbish.R
import com.shuaijun.rubbish.databinding.FragmentCameraBinding
import com.shuaijun.rubbish.databinding.ItemCameraLabelBinding
import com.shuaijun.rubbish.snpe.ImageDetectionFloat
import com.shuaijun.rubbish.snpe.YuvToRgbConverter
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** Helper type alias used for analysis use case callbacks */
typealias LumaListener = (luma: Array<String?>) -> Unit

class CameraFragment : Fragment() {

    private val rotation = Surface.ROTATION_90
    private lateinit var outputDirectory: File
    private lateinit var broadcastManager: LocalBroadcastManager
    private val labelList = mutableListOf<String>()

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var binding: FragmentCameraBinding
    private val adapter: Adapter<String, ItemCameraLabelBinding> by lazy {
        Adapter(labelList, { v, p ->
            v.label.text = labelList[p]
        }, { p ->
            ViewHolder(ItemCameraLabelBinding.inflate(LayoutInflater.from(p.context), p, false))
        })
    }

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    private val mainModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    /** Volume down button receiver used to trigger shutter */
    private val volumeDownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)) {
                // When the volume down button is pressed, simulate a shutter button click
                KeyEvent.KEYCODE_VOLUME_DOWN -> {

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("et_log", "onResume")

        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(
                requireActivity(),
                R.id.fragment_container
            ).navigate(
                CameraFragmentDirections.actionCameraToPermissions()
            )
            return
        }

        setUpCamera()

    }

    override fun onPause() {
        super.onPause()
        Log.d("et_log", "onPause")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        broadcastManager.unregisterReceiver(volumeDownReceiver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentCameraBinding.inflate(inflater, container, false).also {
            binding = it
        }.root

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        broadcastManager = LocalBroadcastManager.getInstance(view.context)
        val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }
        broadcastManager.registerReceiver(volumeDownReceiver, filter)
        outputDirectory = FullscreenActivity.getOutputDirectory(requireContext())

        binding.btnStart.setOnClickListener {
            Toast.makeText(requireContext(), "启动成功", Toast.LENGTH_SHORT).show()
            work = true
        }
        binding.btnStop.setOnClickListener {
            work = false
            Toast.makeText(requireContext(), "识别停止", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var convert: YuvToRgbConverter
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        convert = YuvToRgbConverter(requireContext())
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())

//        val params = binding.viewFinder.layoutParams as ConstraintLayout.LayoutParams
//        val windowManager = requireActivity().windowManager
//        val r = windowManager.defaultDisplay.height * 3 / 4
//        Log.e("et_log", "r = $r")
//        params.height = r
//        params.width = r
//        params.marginEnd = 30
//        binding.viewFinder.layoutParams = params

        binding.categorySpinner.adapter = ArrayAdapter(
            requireActivity(), android.R.layout.simple_list_item_1, listCategory
        )

        binding.categorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    category = position
                    Toast.makeText(
                        requireContext(),
                        "正在分拣:${listCategory.get(position)}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        binding.spinner.adapter = ArrayAdapter(
            requireActivity(), android.R.layout.simple_list_item_1, modelList
        )
        binding.btnHistory.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(CameraFragmentDirections.actionCameraFragmentToHistoryFragment())
        }

    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun setUpCamera() {
        Log.d("et_log", "setUpCamera")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            lensFacing = when {
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases() {
        val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // We request aspect ratio but no resolution to match preview config, but letting
            // CameraX optimize for whatever specific resolution best fits our use cases
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()
            // The analyzer can then be assigned to the instance
            .also { it ->
                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer(convert) { luma ->
                    // Values returned from our analyzer are passed to the attached listener
                    // We log image analysis results here - you should do something useful
                    // instead!

                    if (work) {


                        when (category) {
                            0 -> {
                                createDataInfo(luma, "KitchenWaste")
                            }
                            1 -> {
                                createDataInfo(luma, "Recyclable")
                            }
                            2 -> {
                                createDataInfo(luma, "Other")
                            }
                            3 -> {
                                createDataInfo(luma, "Harmful")
                            }
                        }


                        Log.d(TAG, "Average luminosity: $luma")
                        if (labelList.size > 200) {
                            labelList.iterator().apply {
                                var i = 0
                                while (hasNext()) {
                                    next()
                                    this.remove()
                                    if (i++ > 170) break
                                }
                            }
                            binding.recyclerview.post {
                                adapter.notifyDataSetChanged()
                            }
                        }
                        labelList.add(
                            "${
                                SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm:ss SSS",
                                    Locale.CHINA
                                ).format(System.currentTimeMillis())
                            } ${luma[0]}"
                        )
                    }


                    binding.recyclerview.post {
                        adapter.notifyItemInserted(labelList.size)
                        binding.recyclerview.scrollToPosition(labelList.size - 1)
                    }
                })
            }
        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalyzer
            )
            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(binding.viewFinder.createSurfaceProvider())
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun createDataInfo(luma: Array<String?>, s: String) {
        luma[0]?.let {
            if (it.startsWith(s)) {
                createFile()?.apply {
                    luma.let { it1 ->
                        it1[0]?.let { it2 ->
                            it1[2]?.let { it3 ->
                                DataInfo(
                                    SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm:ss",
                                        Locale.CHINA
                                    ).format(System.currentTimeMillis()),
                                    this.absolutePath,
                                    it2, listCategory[0], it3
                                ).apply {
                                    mainModel.putAnalHistory(this)
                                }
                                takePicture(this)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createFile(): File? {
        return requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let {
            File(it, "${System.currentTimeMillis()}$PHOTO_EXTENSION")
        }
    }

    private fun takePicture(photoFile: File) {
        imageCapture?.let { imageCapture ->
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .build()
            imageCapture.takePicture(
                outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e("TAG", "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                        Log.d("TAG", "Photo capture succeeded: $savedUri")
                    }
                })

//            // We can only change the foreground Drawable using API level 23+ API
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                // Display flash animation to indicate that photo was captured
//                container.postDelayed({
//                    container.foreground = ColorDrawable(Color.WHITE)
//                    container.postDelayed(
//                        { container.foreground = null }, ANIMATION_FAST_MILLIS
//                    )
//                }, ANIMATION_SLOW_MILLIS)
//            }
        }
    }

    /**
     *  [androidx.camera.core.ImageAnalysisConfig] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    /**
     * Our custom image analysis class.
     *
     * <p>All we need to do is override the function `analyze` with our desired operations. Here,
     * we compute the average luminosity of the image by looking at the Y plane of the YUV frame.
     */
    private class LuminosityAnalyzer(
        val convert: YuvToRgbConverter,
        listener: LumaListener? = null
    ) : ImageAnalysis.Analyzer {
        private var bitmapBuffer: Bitmap? = null
        private val frameRateWindow = 8
        private val frameTimestamps = ArrayDeque<Long>(5)
        private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
        private var lastAnalyzedTimestamp = 0L
        var framesPerSecond: Double = -1.0
            private set

        /**
         * Used to add listeners that will be called with each luma computed
         */
        fun onFrameAnalyzed(listener: LumaListener) = listeners.add(listener)

        /**
         * Helper extension function used to extract a byte array from an image plane buffer
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        /**
         * Analyzes an image to produce a result.
         *
         * <p>The caller is responsible for ensuring this analysis method can be executed quickly
         * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
         * images will not be acquired and analyzed.
         *
         * <p>The image passed to this method becomes invalid after this method returns. The caller
         * should not store external references to this image, as these references will become
         * invalid.
         *
         * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
         * call image.close() on received images when finished using them. Otherwise, new images
         * may not be received or the camera may stall, depending on back pressure setting.
         *
         */
        override fun analyze(image: ImageProxy) {
            // If there are no listeners attached, we don't need to perform analysis
            if (listeners.isEmpty()) {
                image.close()
                return
            }

            // Keep track of frames analyzed
            val currentTime = System.currentTimeMillis()
            frameTimestamps.push(currentTime)

            // Compute the FPS using a moving average
            while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
            val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
            val timestampLast = frameTimestamps.peekLast() ?: currentTime
            framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
                    frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

            // Analysis could take an arbitrarily long amount of time
            // Since we are running in a different thread, it won't stall other use cases

            lastAnalyzedTimestamp = frameTimestamps.first

            // Since format in ImageAnalysis is YUV, image.planes[0] contains the luminance plane
            val buffer = image.planes[0].buffer

            // Extract image data from callback object
            val data = buffer.toByteArray()

            // Convert the data into an array of pixel values ranging 0-255
            val pixels = data.map { it.toInt() and 0xFF }
            // Compute average luminance for the image
            val luma = pixels.average()

            image.apply {
                if (bitmapBuffer == null) bitmapBuffer =
                    Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                convert.yuvToRgb(this, bitmapBuffer!!)
                ImageDetectionFloat.getInstance().detection(bitmapBuffer!!) { result ->
                    listeners.forEach {
                        it(result)
                    }
                }
            }
            image.close()
        }
    }

    companion object {

        private const val TAG = "CameraXBasic"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private val listCategory = arrayListOf("厨余垃圾", "可回收类", "其他垃圾", "有害垃圾")
        private val modelList = arrayListOf("浮点模型", "量化模型")
        private var work = false
        private var category: Int = 0

//        /** Helper function used to create a timestamped file */
//        private fun createFile(baseFolder: File, format: String, extension: String) =
//            File(
//                baseFolder, SimpleDateFormat(format, Locale.US)
//                    .format(System.currentTimeMillis()) + extension
//            )
    }
}
