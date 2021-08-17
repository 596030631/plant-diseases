package com.sj.canvas

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sj.canvas.databinding.ActivityLoginBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Consumer
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loading: ProgressDialog
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        loading = ProgressDialog(this)
            .apply {
                setMessage("正在验证")
            }
        loading.window?.setGravity(Gravity.CENTER)
        binding.btnRegisterIn.setOnClickListener {
            val inputUser = binding.inputUser.text
            val inputPasswd = binding.inputPasswd.text
            if (TextUtils.isEmpty(inputUser)) {
                Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(inputPasswd)) {
                Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val notExist = TextUtils.isEmpty(sharedPreferences.getString(inputUser.toString(), ""))
            if (notExist) {
                sharedPreferences.edit().putString(inputUser.toString(), inputPasswd.toString())
                    .apply()
                Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "账号已存在", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnSignIn.setOnClickListener {
            val inputUser = binding.inputUser.text
            val inputPasswd = binding.inputPasswd.text
            if (TextUtils.isEmpty(inputUser)) {
                Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(inputPasswd)) {
                Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loading.show()
            binding.btnSignIn.isEnabled = false
            Single.timer(1500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    Consumer {
                        binding.btnSignIn.isEnabled = true
                        val passwdSaved = sharedPreferences.getString(inputUser.toString(), "")
                        loading.dismiss()
                        if (!TextUtils.equals(inputPasswd, passwdSaved)) {
                            Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show()
                        } else {
                            Intent(this, MainActivity::class.java).apply {
                                startActivity(this)
                            }
                            finish()
                        }
                    })
        }

        if (!hasPermissions(this)) {
            // Request camera-related permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
            }
        }
        sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE)
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 10
        private val PERMISSIONS_REQUIRED = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        /** Convenience method used to check if all permissions required by this app are granted */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

}