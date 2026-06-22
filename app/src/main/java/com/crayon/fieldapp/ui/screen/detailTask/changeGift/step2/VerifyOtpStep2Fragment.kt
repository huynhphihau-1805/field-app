package com.crayon.fieldapp.ui.screen.detailTask.changeGift.step2

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentVerifyOtpStep2Binding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.dialog.OtpFailedDialog
import com.crayon.fieldapp.ui.base.dialog.OtpSuccessDialog
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.Utils
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showMessageDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class VerifyOtpStep2Fragment(val onNextClick: () -> Unit = {}) :
    BaseFragment<FragmentVerifyOtpStep2Binding, VerifyOtpStep2ViewModel>() {
    override val layoutId: Int = R.layout.fragment_verify_otp_step2
    override val viewModel: VerifyOtpStep2ViewModel by viewModel()
    private var taskId: String? = null
    private var _phone: String? = null
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskId = requireArguments().getString("taskId").toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnResend.visibility = View.VISIBLE
        binding.btnResend.isEnabled = false
        binding.btnResend.setTextColor(resources.getColor(R.color.colorGrayDisable, null))

        timer = startLoginTimer(binding.txtTime, {
            binding.btnResend.isEnabled = true
            binding.btnResend.setTextColor(resources.getColor(R.color.colorAccent, null))
            binding.txtTime.visibility = View.GONE
        }, {})

        binding.btnResend.setSingleClick {
            binding.btnResend.isClickable = false
            binding.btnResend.setTextColor(resources.getColor(R.color.colorGrayDisable, null))
            binding.txtTime.visibility = View.VISIBLE
            if (taskId != null && _phone != null) {
                timer?.start()
                viewModel.resendOtpCustomer(taskId = taskId.toString(), phone = _phone.toString())
            }
        }

        binding.btnNext.setSingleClick {
            val otp = binding.edtOtp.text.toString()
            if (otp.isBlank() || otp.length < 6) {
                binding.edtOtp.setError("Vui lòng nhập mã OTP")
                return@setSingleClick
            }
            Utils.hideKeyboard(requireActivity())
            if (taskId != null && _phone != null) {
                viewModel.verifyCustomerOtp(taskId.toString(), _phone.toString(), otp)
            }
        }

        viewModel.verifyCustomerOtp.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        timer?.cancel()
                        it.data?.let {
                            val dialog = OtpSuccessDialog({
                                onNextClick.invoke()
                            })
                            dialog.show(childFragmentManager, dialog.tag)

                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                        val dialog = OtpFailedDialog({

                        })
                        dialog.show(childFragmentManager, dialog.tag)
                    }
                }
            }
        })

        viewModel.resendOtpCustomer.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        requireContext().showMessageDialog(
                            title = it.data?.message.toString()
                        )
                        timer?.start()
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })
    }


    override fun onResume() {
        super.onResume()
        timer?.start()
    }

    override fun onPause() {
        super.onPause()
        timer?.cancel()
    }

    fun startLoginTimer(
        timeView: TextView,
        onFinish: () -> Unit,
        timeDown: (time: String) -> Unit
    ): CountDownTimer {
        return object : CountDownTimer(TIMER_RESEND_OTP, 1000L) {
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
        }
    }

    fun setCustomerPhone(mPhone: String) {
        this._phone = mPhone
//        binding.txtHintOtp.text = "Mã OTP được gửi đến số " + this._phone
    }

    companion object {
        const val TIMER_RESEND_OTP = 120000L
    }
}