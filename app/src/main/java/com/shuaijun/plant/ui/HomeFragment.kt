package com.shuaijun.plant.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.shuaijun.plant.R
import com.shuaijun.plant.databinding.HomeFragmentBinding
import com.shuaijun.plant.snpe.ImageDetectionFloat
import com.shuaijun.plant.snpe.LoadModelTask
import java.io.File

class HomeFragment : BaseFragment() {

    private lateinit var viewModel: HomeViewModel

    //    private lateinit var binding: FragmentStreamDetectorBinding
    private lateinit var binding: HomeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        binding = FragmentStreamDetectorBinding.inflate(inflater, container,false)
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        binding.floatingActionButton.setOnClickListener {
//            val intent = Intent(activity, CameraActivity::class.java)
//            startActivity(intent)
            mainModel.fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CameraFragment.newInstance()).commit()
        }
    }

    override fun onResume() {
        super.onResume()





//        DetectionApi.getInstance().deInit()
//        DetectionApi.getInstance().initModel(requireContext())
//        val bitmap = FileUtils.readBitmapFromFile(
//            activity!!.assets,
//            "citrus_anth1.jpg"
//        )
//        bitmap?.let {
//            DetectionApi.getInstance().detection(bitmap, bitmap.width, bitmap.height) {
//                var objectCount = 0
//                if (it != null) {
//                    objectCount = it.size
//                }
//                if (it != null && it.isNotEmpty()) {
//                    Log.d(
//                        TAG,
//                        "detect object size " + it.size
//                    )
//                    val mPaint = Paint()
//                    mPaint.setARGB(255, 0, 255, 0)
//                    mPaint.isFilterBitmap = true
//                    mPaint.style = Paint.Style.STROKE
//                    mPaint.color = Color.RED
//                    val scaleBitmap2: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
//                    val canvas = Canvas(scaleBitmap2)
//                    val rect = ArrayList<Rect>()
//                    for (i in it.indices) {
//                        rect.add(
//                            Rect(
//                                it[i].x1.toInt(),
//                                it[i].y1.toInt(),
//                                it[i].x2.toInt(),
//                                it[i].y2.toInt()
//                            )
//                        )
//                    }
//                    for (i in rect.indices) {
//                        Log.d(
//                            TAG,
//                            "rect " + rect[i]
//                        )
//                        val rect = rect[i]
//                        mPaint.setARGB(255, 0, 255, 0)
//                        canvas.drawRect(rect, mPaint)
//                        val info: ObjectInfo = it[i]
//                        if (info.class_id < ObjectDetectorSSD.label_list.size) {
//                            canvas.drawText(
//                                String.format(
//                                    "%s : %f",
//                                    ObjectDetectorSSD.label_list.get(info.class_id),
//                                    info.score
//                                ), rect.left.toFloat(), (rect.top - 5).toFloat(), mPaint
//                            )
//                        }
//                    }
//                    binding.source.post {
//                        binding.source.setImageBitmap(scaleBitmap2)
//                        binding.source.draw(canvas)
//                    }
//                }
//                Log.d(TAG, "AAAAA" + Arrays.toString(it))
//            }
//        }
    }

    companion object {
        const val TAG = "HomeFragment"
        const val id = 1

        @JvmStatic
        fun newInstance() = HomeFragment()
    }

}