package com.sj.plant

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.sj.plant.databinding.ActivityLoginBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Consumer
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loading: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        loading = ProgressDialog(this)
            .apply {
                setMessage("正在验证")
            }
        loading.window?.setGravity(Gravity.CENTER)
        binding.btnSignIn.setOnClickListener {
            loading.show()
            binding.btnSignIn.isEnabled = false
            Single.timer(1500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    Consumer {
                        loading.dismiss()
                        Intent(this, MainActivity::class.java).apply {
                            startActivity(this)
                        }
                        finish()
                    })
        }
    }

    override fun onResume() {
        super.onResume()

    }

}