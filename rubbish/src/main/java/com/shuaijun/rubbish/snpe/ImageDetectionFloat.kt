package com.shuaijun.rubbish.snpe

import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import android.util.Pair
import com.orhanobut.logger.Logger
import com.qualcomm.qti.snpe.FloatTensor
import com.qualcomm.qti.snpe.Tensor
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.pow

class ImageDetectionFloat {

    fun available() = !detectLock.get()

    fun detection(image: Bitmap, call: (Array<String?>) -> Unit) {
        synchronized(detectLock) {
            if (!available()) return
            detectLock.set(true)
        }
        Logger.d("开始识别图片")
        LoadModelTask.getInstance().neuralNetwork?.let { it ->
            Logger.d("网络已加载")
            val inputNames: Set<String> = it.inputTensorsNames
            val outputNames: Set<String> = it.outputTensorsNames
            check(!(inputNames.size != 1 || outputNames.size != 1)) { "Invalid network input and/or output tensors." }
            val mInputLayer: String = inputNames.iterator().next()
            val mOutputLayer = outputNames.iterator().next()
            val shapeMap: Map<String, IntArray> = it.inputTensorsShapes
            val ints: IntArray = shapeMap[mInputLayer] ?: return
            val tensor: FloatTensor = it.createFloatTensor(*ints)
            val scaleImage = Bitmap.createScaledBitmap(image, 224, 224, true)
            val dimensions = tensor.shape
            val rgbBitmapAsFloat: FloatArray = loadRgbBitmapAsFloat(scaleImage)
            tensor.write(rgbBitmapAsFloat, 0, rgbBitmapAsFloat.size)
            val inputs: MutableMap<String, FloatTensor> = HashMap()
            inputs[mInputLayer] = tensor
            val javaExecuteStart = SystemClock.elapsedRealtime()
            val outputs: Map<String, FloatTensor> = it.execute(inputs)
            val javaExecuteEnd = SystemClock.elapsedRealtime()
            val mJavaExecuteTime = javaExecuteEnd - javaExecuteStart

            val labels = LoadModelTask.getInstance().labels
            for ((key, outputTensor) in outputs) {
                if (key == mOutputLayer) {
                    val array = FloatArray(outputTensor.size)
                    outputTensor.read(array, 0, array.size)
                    var sum: Double = 0.0
                    val topArray = topK(10, array)
                    for (pair in topArray) {
                        sum += Math.E.pow(pair.second.toDouble())
                    }

                    call(
                        arrayOf(
                            labels[topArray[0].first],
                            "${Math.E.pow(topArray[0].second.toDouble()) / sum}",
                            String.format("%03d", mJavaExecuteTime / 3)
                        )
                    )
                }
            }
            releaseTensors(inputs, outputs)
            detectLock.set(false)
        }
    }

    private fun topK(k: Int, tensor: FloatArray): MutableList<Pair<Int, Float>> {
        val selected = BooleanArray(tensor.size)
        val topK = mutableListOf<Pair<Int, Float>>()
        var count = 0
        while (count < k) {
            val index: Int = top(tensor, selected)
            selected[index] = true
            Log.d(TAG, "index=${index}  tensor.size=${tensor.size}")
            val pair = Pair(index, tensor[index])
            topK.add(pair)
            count++
        }
        return topK
    }

    private fun top(array: FloatArray, selected: BooleanArray): Int {
        var index = 0
        var max = -1f
        for (i in array.indices) {
            if (selected[i]) {
                continue
            }
            if (array[i] > max) {
                max = array[i]
                index = i
            }
        }
        return index
    }


    private fun releaseTensors(vararg tensorMaps: Map<String, Tensor>) {
        for (tensorMap in tensorMaps) {
            for (tensor in tensorMap.values) {
                tensor.release()
            }
        }
    }

    private fun loadRgbBitmapAsFloat(image: Bitmap): FloatArray {
        val pixels = IntArray(image.width * image.height)
        image.getPixels(
            pixels, 0, image.width, 0, 0,
            image.width, image.height
        )
        val pixelsBatched = FloatArray(pixels.size * 3)
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val idx = y * image.width + x
                val batchIdx = idx * 3
                val rgb: FloatArray = extractColorChannels(pixels[idx]) ?: continue
                pixelsBatched[batchIdx] = rgb[0]
                pixelsBatched[batchIdx + 1] = rgb[1]
                pixelsBatched[batchIdx + 2] = rgb[2]
            }
        }
        return pixelsBatched
    }

    private fun extractColorChannels(pixel: Int): FloatArray? {
        val b = (pixel and 0xFF).toFloat()
        val g = (pixel shr 8 and 0xFF).toFloat()
        val r = (pixel shr 16 and 0xFF).toFloat()
        return floatArrayOf(preProcess(r), preProcess(g), preProcess(b))
    }

    private fun preProcess(original: Float): Float {
        return original
    }

    fun getMin(array: FloatArray): Float {
        var min = Float.MAX_VALUE
        for (value in array) {
            if (value < min) {
                min = value
            }
        }
        return min
    }

    fun getMax(array: FloatArray): Float {
        var max = Float.MIN_VALUE
        for (value in array) {
            if (value > max) {
                max = value
            }
        }
        return max
    }


    companion object {
        private const val TAG = "ImageDetectionFloat"

        private val detectLock: AtomicBoolean = AtomicBoolean(false)

        @JvmStatic
        fun getInstance(): ImageDetectionFloat {
            return Holder.ins
        }
    }

    private object Holder {
        val ins = ImageDetectionFloat()
    }
}