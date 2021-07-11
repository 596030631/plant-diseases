//package com.shuaijun.rubbish.snpe
//
//import android.graphics.BitmapFactory
//import com.orhanobut.logger.Logger
//
//class SnpeHelper {
//    fun use() {
//        val bitmap = BitmapFactory.decodeFile(pathName)
//        bitmap?.let { bp ->
//            ImageDetectionFloat.getInstance().detection(bp) {
//                Logger.w(it.toString())
//                listener?.analysis(id, it.toString())
//            }
//        }
//    }
//
//}