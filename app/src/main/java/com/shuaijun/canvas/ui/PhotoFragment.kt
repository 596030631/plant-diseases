/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shuaijun.canvas.ui

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.kaopiz.kprogresshud.KProgressHUD
import com.orhanobut.logger.Logger
import com.shuaijun.canvas.ImageInputData
import com.shuaijun.canvas.R
import java.io.File


/** Fragment used for each individual page showing a photo inside of [GalleryFragment] */
class PhotoFragment internal constructor() : BaseFragment() {

    private lateinit var loading: KProgressHUD
    private lateinit var view: ImageView
    private var resultString: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ImageView(context).also { view = it }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Logger.d("onActivityCreate")
        loading = KProgressHUD.create(requireContext())
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setLabel("请稍等")
            .setDetailsLabel("正在分析")
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        mainModel.analysisImageResult.observe(viewLifecycleOwner, { result ->
            loading.dismiss()
            resultString = result.result

            if (hashCode().toLong() == result.id) {
                val html = "<h1>识别结果</h1></br><h2>$result</h2>"
                AlertDialog.Builder(requireContext())
                    .setNegativeButton(
                        R.string.confirm
                    ) { p0, _ ->
                        Logger.d("onCLick")
                        p0?.dismiss()
                        Toast.makeText(requireContext(), "PDF检测结果已生成", Toast.LENGTH_LONG).show()
                    }
                    .setIcon(R.drawable.ic_report_blue)
                    .setTitle("推理由Qualcomm-CPU完成")
                    .setMessage(Html.fromHtml(html))
                    .setCancelable(true)
                    .create().show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val args = arguments ?: return
        val resource = args.getString(FILE_NAME_KEY)?.let {
            File(it).also { file ->
                if (resultString == null) {
                    loading.show()
                    ImageInputData(hashCode().toLong(), file.absolutePath, "").also { id ->
                        mainModel.analysisImage.postValue(id)
                    }
                }
            }
        } ?: R.drawable.ic_photo
        Glide.with(view).load(resource).into(view)
        Logger.d("onResume")
    }

    override fun onPause() {
        super.onPause()
        Logger.d("onPause")

        if (loading.isShowing) loading.dismiss()
    }

    companion object {
        private const val FILE_NAME_KEY = "file_name"

        fun create(image: File) = PhotoFragment().apply {
            arguments = Bundle().apply {
                putString(FILE_NAME_KEY, image.absolutePath)
            }
        }
    }
}