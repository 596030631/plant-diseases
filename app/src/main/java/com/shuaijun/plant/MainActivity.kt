package com.shuaijun.plant

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.shuaijun.plant.databinding.ActivityMainBinding
import com.shuaijun.plant.snpe.SnpeTaskService
import com.shuaijun.plant.ui.HomeFragment


class MainActivity : FragmentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainModel: MainViewModel
    private var snpeService: ISnpeService? = null
    private var serviceConnect: ServiceConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.addLogAdapter(AndroidLogAdapter())

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainModel = ViewModelProvider(this).get(MainViewModel::class.java)

        if (Build.VERSION.SDK_INT >= 23) {
            Log.d(tag, "begin askForPermission the sdk version is" + Build.VERSION.SDK_INT)
            askForPermission()
        } else {
            Log.d(tag, "no need to askForPermission the sdk version is" + Build.VERSION.SDK_INT)
            updateUI()
        }

        mainModel.fragmentManager = supportFragmentManager
    }

    private fun askForPermission() {
        //检测权限
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) !== PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) !== PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) !== PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(tag, "didnt get permission,ask for it!")
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                FACE_PERMISSION_QUEST_CAMERA
            )
        } else {
            updateUI()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            FACE_PERMISSION_QUEST_CAMERA ->                 //If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.i(tag, "get camera permission!")
                        if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                            //permission was granted，yay！Do the
                            // mic-related task u need to do.
                            Log.i(tag, "get mic permission!")
                            if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                                Log.i(tag, "get read_phone permission!")
                                Log.i(tag, "get all permission! Go on Verify!")
                                if (grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                                    updateUI()
                                    return
                                } else {
                                    askReadPhonePermissionError()
                                    return
                                }
                            } else {
                                askReadPhonePermissionError()
                                return
                            }
                        } else {
                            askAudioPermissionError()
                            return
                        }
                    } else {
                        askCameraPermissionError()
                        return
                    }
                }
        }
    }

    private fun updateUI() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment.newInstance()).commit()

        serviceConnect = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Logger.d("连接到服务端")
                snpeService = ISnpeService.Stub.asInterface(service)
                mainModel.analysisImage.observe(this@MainActivity, {
                    snpeService?.putTask(it)
                })
                snpeService?.observer(object : IResult.Stub() {
                    override fun analysis(result: String?) {
                        mainModel.analysisImageResult.postValue(result)
                    }
                })
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Logger.d("服务端断开")
            }
        }
        val intent = Intent(this, SnpeTaskService::class.java)
        bindService(intent, serviceConnect ?: return, BIND_AUTO_CREATE)
    }

    private fun askCameraPermissionError() {
        Log.e(tag, "Didn't get camera permission!")
        val msg = "用户没有授权相机权限"
        askPermissionError(msg)
    }

    private fun askAudioPermissionError() {
        Log.e(tag, "Didn't get mic permission!")
        val msg = "用户没有授权录音权限"
        askPermissionError(msg)
    }

    private fun askReadPhonePermissionError() {
        Log.e(tag, "Didn't get read_phone permission!")
        val msg = "用户没有授权读取手机状态权限"
        askPermissionError(msg)
    }

    private fun askPermissionError(msg: String) {
        Log.w(tag, "设备授权验证失败")
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnect ?: return)
    }


    companion object {
        private const val tag = "MainActivity"
        private const val FACE_PERMISSION_QUEST_CAMERA = 1024
    }

}