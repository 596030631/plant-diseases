package com.shuaijun.plant.ui

import android.animation.Animator
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.kaopiz.kprogresshud.KProgressHUD
import com.shuaijun.plant.R
import com.shuaijun.plant.databinding.FragmentInletsBinding
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class InletsFragment : BaseFragment() {

    private lateinit var binding: FragmentInletsBinding
    private lateinit var execute: Executor
    private lateinit var loading: KProgressHUD
//    private lateinit var loading: AlertDialog
//    private lateinit var loadingBinding: LoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentInletsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        execute = Executors.newSingleThreadExecutor()
        binding.inputUserLayout.isCounterEnabled = true
        binding.inputPasswdLayout.isCounterEnabled = true

        loading = KProgressHUD.create(requireContext())
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setLabel("Please wait")
            .setDetailsLabel("正在验证")
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)

        binding.login.setOnClickListener {
            val user = binding.inputUser.text.toString()
            val passwd = binding.inputPasswd.text.toString()
            if (TextUtils.isEmpty(user)) {
                binding.inputUserLayout.error = "用户名不能为空"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(passwd)) {
                binding.inputPasswdLayout.error = "密码不能为空"
                return@setOnClickListener
            }
            showLoading()

            execute.execute {
                SystemClock.sleep(2_000)
                mainModel.putString("user", user)
                mainModel.putString("passwd", passwd)

                val userSaved = mainModel.getString("user", "user")
                val passwdSaved = mainModel.getString("passwd", "passwd")

                val success =
                    TextUtils.equals(user, userSaved) && TextUtils.equals(passwd, passwdSaved)
                if (success) {
                    showSuccess()
                    mainModel.fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment.newInstance()).commit()
                } else showFailed()
                hideLoading()
            }
        }
    }

    private fun showLoading() {
        if (loading.isShowing) return
        loading.show()
    }

    private fun hideLoading() {
        requireActivity().runOnUiThread {
            if (loading.isShowing) loading.dismiss()
        }
    }

    private fun showSuccess() {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), "登陆成功", Toast.LENGTH_LONG).show()
        }
    }

    private fun showFailed() {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), "验证失败", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        beforeLoading()
    }

    private fun beforeLoading() {
        binding.login.visibility = View.GONE
        binding.layoutInput.visibility = View.GONE
    }

    private fun afterLoading() {
        binding.login.visibility = View.VISIBLE
        binding.layoutAnim.visibility = View.GONE
        binding.layoutInput.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        binding.animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                afterLoading()

            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationRepeat(animation: Animator?) {

            }

        })
    }

    companion object {
        @JvmStatic
        fun newInstance() = InletsFragment()
    }
}