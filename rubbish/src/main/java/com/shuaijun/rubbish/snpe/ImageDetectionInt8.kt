package com.shuaijun.rubbish.snpe

import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import android.util.Pair
import com.orhanobut.logger.Logger
import com.qualcomm.qti.snpe.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class ImageDetectionInt8 {

    fun available() = !detectLock.get()

    fun detection(image: Bitmap, call: (MutableList<String>) -> Unit) {
        synchronized(detectLock) {
            if (!available()) return
            detectLock.set(true)
        }
        LoadModelTask.getInstance().neuralNetwork?.let { neural ->
            val inputNames: Set<String> = neural.inputTensorsNames
            val outputNames: Set<String> = neural.outputTensorsNames
            check(!(inputNames.size != 1 || outputNames.size != 1)) { "Invalid network input and/or output tensors." }
            val mInputLayer: String = inputNames.iterator().next()
            val mOutputLayer = outputNames.iterator().next()
            val result: MutableList<String> = LinkedList()

            val inputTensors: MutableMap<String, TF8UserBufferTensor> = HashMap()
            val outputTensors: MutableMap<String, TF8UserBufferTensor> = HashMap()

            val inputBuffers: MutableMap<String, ByteBuffer> = HashMap()
            val outputBuffers: MutableMap<String, ByteBuffer> = HashMap()
            val inputAttributes: TensorAttributes = neural.getTensorAttributes(mInputLayer)
            val inputParams = resolveTf8Params(inputAttributes)
            inputBuffers[mInputLayer] =
                ByteBuffer.allocateDirect(inputParams.size).order(ByteOrder.nativeOrder())
            val scaleImage = Bitmap.createScaledBitmap(image, 224, 224, true)
            val rgbBitmapAsFloat: FloatArray = loadRgbBitmapAsFloat(scaleImage)

            val inputBuffer = inputBuffers[mInputLayer] ?: return
            quantize(rgbBitmapAsFloat, inputBuffer, inputParams)

            inputTensors[mInputLayer] = neural.createTF8UserBufferTensor(
                inputParams.size, inputParams.strides,
                inputParams.stepExactly0, inputParams.stepSize,
                inputBuffers[mInputLayer]
            )

            val outputAttributes: TensorAttributes = neural.getTensorAttributes(mOutputLayer)
            val outputParams = resolveTf8Params(outputAttributes)
            outputParams.stepExactly0 = mStepExactly0
            outputParams.stepSize = mStepSize
            outputBuffers[mOutputLayer] =
                ByteBuffer.allocateDirect(outputParams.size).order(ByteOrder.nativeOrder())
            outputTensors[mOutputLayer] = neural.createTF8UserBufferTensor(
                outputParams.size, outputParams.strides,
                outputParams.stepExactly0, outputParams.stepSize,
                outputBuffers[mOutputLayer]
            )

            val javaExecuteStart = SystemClock.elapsedRealtime()
            val status = neural.execute(inputTensors, outputTensors)
            val javaExecuteEnd = SystemClock.elapsedRealtime()
            if (!status) return
            var mJavaExecuteTime = javaExecuteEnd - javaExecuteStart
            val outputValues = deQuantize(outputTensors[mOutputLayer], outputBuffers[mOutputLayer])

            val labels = LoadModelTask.getInstance().labels

            for (pair in outputValues?.let { topK(k = 1, tensor = it) }!!) {
                labels[pair.first]?.let { result.add(it) }
                result.add(pair.second.toString())
            }
            val resultString = result.toTypedArray()
            Logger.d(resultString)
            releaseTensors(inputTensors, outputTensors)
            detectLock.set(false)
        }
    }

    private fun topK(k: Int = 1, tensor: FloatArray): MutableList<Pair<Int, Float>> {
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


    private fun releaseTensors(vararg tensorMaps: Map<String, UserBufferTensor>) {
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

    private fun quantize(src: FloatArray, dst: ByteBuffer, tf8Params: Tf8Params) {
        val encoding = getTf8Encoding(src)
        val quantized = ByteArray(src.size)
        for (i in src.indices) {
            var data = src[i].coerceAtMost(encoding.max).coerceAtLeast(encoding.min)
            data = data / encoding.delta - encoding.offset
            quantized[i] = Math.round(data).toByte()
        }
        dst.put(quantized)
        tf8Params.stepSize = encoding.delta
        tf8Params.stepExactly0 = Math.round(-encoding.min / encoding.delta)
    }

    private fun getTf8Encoding(array: FloatArray): Tf8Encoding {
        val encoding = Tf8Encoding()
        val num_steps = Math.pow(2.0, TF8_BITWIDTH.toDouble()).toInt() - 1
        val new_min = Math.min(getMin(array), 0f)
        var new_max = Math.max(getMax(array), 0f)
        val min_range = 0.1f
        new_max = Math.max(new_max, new_min + min_range)
        encoding.delta = (new_max - new_min) / num_steps
        if (new_min < 0 && new_max > 0) {
            var quantized_zero = Math.round(-new_min / encoding.delta).toFloat()
            quantized_zero =
                Math.min(num_steps.toDouble(), Math.max(0.0, quantized_zero.toDouble()))
                    .toFloat()
            encoding.offset = -quantized_zero
        } else {
            encoding.offset = Math.round(new_min / encoding.delta).toFloat()
        }
        encoding.min = encoding.delta * encoding.offset
        encoding.max = encoding.delta * num_steps + encoding.min
        return encoding
    }

    private fun deQuantize(tensor: TF8UserBufferTensor?, buffer: ByteBuffer?): FloatArray? {
        if (buffer == null || tensor == null) return null
        val outputSize: Int = buffer.capacity()
        val quantizedArray = ByteArray(outputSize)
        buffer.get(quantizedArray)
        val dequantizedArray = FloatArray(outputSize)
        for (i in 0 until outputSize) {
            val quantizedValue = quantizedArray[i].toInt() and 0xFF
            dequantizedArray[i] = tensor.min + quantizedValue * tensor.quantizedStepSize
        }
        return dequantizedArray
    }

    private fun resolveTf8Params(attribute: TensorAttributes): Tf8Params {
        val rank = attribute.dims.size
        val strides = IntArray(rank)
        strides[rank - 1] = TF8_SIZE
        for (i in rank - 1 downTo 1) {
            strides[i - 1] = strides[i] * attribute.dims[i]
        }
        var bufferSize = TF8_SIZE
        for (dim in attribute.dims) {
            bufferSize *= dim
        }
        return Tf8Params(bufferSize, strides)
    }

    private class Tf8Params(var size: Int, var strides: IntArray) {
        var stepExactly0 = 0
        var stepSize = 0f
    }

    private class Tf8Encoding {
        var min = 0f
        var max = 0f
        var delta = 0f
        var offset = 0f
    }


    private fun getMin(array: FloatArray): Float {
        var min = Float.MAX_VALUE
        for (value in array) {
            if (value < min) {
                min = value
            }
        }
        return min
    }

    private fun getMax(array: FloatArray): Float {
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

        private const val TF8_SIZE = 1

        private const val TF8_BITWIDTH = 8

        private const val mStepExactly0 = 0

        private const val mStepSize = 1.0f

        private val detectLock: AtomicBoolean = AtomicBoolean(false)

        @JvmStatic
        fun getInstance(): ImageDetectionInt8 {
            return Holder.ins
        }
    }

    private object Holder {
        val ins = ImageDetectionInt8()
    }
}