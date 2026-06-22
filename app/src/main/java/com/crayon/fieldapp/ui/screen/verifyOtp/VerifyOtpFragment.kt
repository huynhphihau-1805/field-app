package com.crayon.fieldapp.ui.screen.verifyOtp

import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentVerifyOtpBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showMessageDialog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.UUID
import java.util.concurrent.TimeUnit

class VerifyOtpFragment : BaseFragment<FragmentVerifyOtpBinding, VerifyOtpViewModel>() {

    override val layoutId: Int = R.layout.fragment_verify_otp

    override val viewModel: VerifyOtpViewModel by viewModel()
    lateinit var phone: String
    lateinit var password: String
    lateinit var device_id: String
    lateinit var fcm_token: String
    private var mMode = VERIFY_LOGIN_MODE
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        phone = requireArguments().get("phone").toString()
        password = requireArguments().get("password").toString()
        mMode = requireArguments().getInt("mode")

        device_id = Settings.Secure.getString(
            requireActivity().contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmit.setOnClickListener {
            viewModel.apply {
                showLoadingDialog()
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    dismissLoadingDialog()
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }
                    val token = task.result
                    fcm_token = token.toString()
                    if (binding.edtOtp.text.toString().isNullOrEmpty()) {
                        binding.edtOtp.setError("Vui lòng nhập mã OTP")
                        return@OnCompleteListener
                    }
                    if (mMode == VERIFY_LOGIN_MODE) {
                        clickVerifyOtp(
                            phone,
                            binding.edtOtp.text.toString(),
                            device_id,
                            token.toString()
                        )
                    } else {
                        clickVerifyOtpPassword(
                            phone,
                            binding.edtOtp.text.toString()
                        )
                    }
                })
            }
        }
        binding.txtResend.visibility = View.VISIBLE

        timer = startLoginTimer(binding.time, {
            binding.txtResend.isEnabled = true
            binding.time.visibility = View.GONE
        }, {})

        binding.txtResend?.setSingleClick {
            if (mMode == VERIFY_LOGIN_MODE) {
                binding.txtResend.isEnabled = false
                binding.time.visibility = View.VISIBLE
                viewModel?.resendOtp(phone, password)
            } else {
                viewModel?.resendOtpPassword(phone)
            }
        }


        viewModel.apply {
            isVerifySuccess.observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let {
                    when (it.status) {
                        Status.LOADING -> {
                            showLoadingDialog()
                        }

                        Status.SUCCESS -> {
                            dismissLoadingDialog()
                            if (timer != null) {
                                timer?.cancel()
                            }
                            if (mMode == VERIFY_LOGIN_MODE) {
                                findNavController().navigate(R.id.action_verify_to_main)
                            } else {
                                val bundel = bundleOf(
                                    "otp" to binding.edtOtp.text.toString().trim(),
                                    "phone" to phone,
                                    "password" to password,
                                    "device_id" to device_id,
                                    "fcm_token" to fcm_token
                                )
                                findNavController().navigate(
                                    R.id.action_verify_to_resetPasswordFragment,
                                    bundel
                                )
                            }
                        }

                        Status.ERROR -> {
                            dismissLoadingDialog()
                        }
                    }
                }
            })

            isReSendOtpLoginSuccess.observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let {
                    when (it.status) {
                        Status.LOADING -> {
                            showLoadingDialog()
                        }

                        Status.SUCCESS -> {
                            dismissLoadingDialog()
                            requireContext().showMessageDialog(
                                title = "Đã gửi lại mã OTP?"
                            )
                            timer?.start()
                        }

                        Status.ERROR -> {
                            dismissLoadingDialog()
                        }
                    }
                }
            })

            isResendOtpOfForgotPasswordSuccess.observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let {
                    when (it.status) {
                        Status.LOADING -> {
                            showLoadingDialog()
                        }

                        Status.SUCCESS -> {
                            dismissLoadingDialog()
                            requireContext().showMessageDialog(
                                title = "Đã gửi lại mã OTP?"
                            )
                            timer?.start()

                        }

                        Status.ERROR -> {
                            dismissLoadingDialog()
                        }
                    }
                }
            })
        }
    }

    fun startLoginTimer(
        timeView: TextView,
        onFinish: () -> Unit,
        timeDown: (time: String) -> Unit
    ): CountDownTimer {
        return object : CountDownTimer(120000, 1000L) {
            override fun onFinish() {
                timeView?.text = "00 : 00"
                onFinish.invoke()
            }

            override fun onTick(millisUntilFinished: Long) {
                timeView?.visibility = View.VISIBLE
                timeView?.text = String.format(
                    "%02d : %02d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                    millisUntilFinished
                                )
                            )
                )

                timeDown.invoke(
                    String.format(
                        " %02d : %02d ",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(
                                        millisUntilFinished
                                    )
                                )
                    )
                )
            }
        }.start()
    }

    companion object {
        const val VERIFY_LOGIN_MODE = 0
        const val VERIFY_RESET_PASSWORD_MODE = 1
    }
}