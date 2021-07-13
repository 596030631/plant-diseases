package com.shuaijun.rubbish.snpe

import android.app.Application
import android.content.Context
import android.util.Log
import com.qualcomm.qti.snpe.NeuralNetwork
import com.qualcomm.qti.snpe.NeuralNetwork.RuntimeCheckOption
import com.qualcomm.qti.snpe.SNPE.NeuralNetworkBuilder
import java.util.*
import kotlin.ConcurrentModificationException

class LoadModelTask {

    var labels = arrayListOf<String?>()
    var neuralNetwork: NeuralNetwork? = null

    fun loadNetwork(ctx: Application) {
        val labelInput = ctx.assets.open("labels.txt")
        labelInput.bufferedReader().useLines { label ->
            label.forEach { line ->
                Log.d("et_log", line)
                labels.add(line)
            }
        }
        labelInput.close()
        val modelInput = ctx.assets.open("effect.dlc")
        val builder = NeuralNetworkBuilder(ctx)
            .setDebugEnabled(false)
            .setRuntimeOrder(NeuralNetwork.Runtime.CPU)
            .setModel(modelInput, modelInput.available())
            .setCpuFallbackEnabled(true)
            .setUseUserSuppliedBuffers(false) // float or int8
            .setUnsignedPD(false)

        modelInput.close()

        neuralNetwork = builder?.build()
        Log.d("LOG_TAG", "input = " + neuralNetwork?.inputTensorsNames)
        Log.d("LOG_TAG", "output = " + neuralNetwork?.outputTensorsNames)
    }

    fun getSupportedRuntimes(
        application: Application,
        mUnsignedPD: Boolean
    ): List<NeuralNetwork.Runtime> {
        val result: MutableList<NeuralNetwork.Runtime> = LinkedList()
        val builder = NeuralNetworkBuilder(application)
        var runtimeCheck = RuntimeCheckOption.NORMAL_CHECK
        if (mUnsignedPD) {
            runtimeCheck = RuntimeCheckOption.UNSIGNEDPD_CHECK
        }
        builder.setRuntimeCheckOption(runtimeCheck)
        for (runtime in NeuralNetwork.Runtime.values()) {
            if (builder.isRuntimeSupported(runtime)) {
                result.add(runtime)
            }
        }
        log(result.toString())
        return result
    }

    companion object {
        private const val TAG = "LoadModelTask"

        @JvmStatic
        fun getInstance() = Holder.ins

        fun log(s: String) {
            Log.d(TAG, s)
        }
    }

    object Holder {
        val ins = LoadModelTask()
    }
}