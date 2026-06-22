package com.crayon.fieldapp.ui.screen.forgot

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentForgotBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.verifyOtp.VerifyOtpFragment
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showDialog
import com.crayon.fieldapp.utils.showMessageDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgotPasswordFragment : BaseFragment<FragmentForgotBinding, ForgotPasswordViewModel>() {

    override val layoutId: Int = R.layout.fragment_forgot

    override val viewModel: ForgotPasswordViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bnReset.setSingleClick {
            val email = binding.edtPhone.text.toString()
            if (email.isBlank()) {
                context?.showMessageDialog(message = "Vui lòng nhập số điện thoại của bạn") { }
            } else {
                viewModel.resetPassword(email)
            }
        }

        viewModel.isForgotPasswordSuccess.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.LOADING -> {
                    viewModel.showLoading()
                }
                Status.SUCCESS -> {
                    viewModel.hideLoading()
                    requireContext().showDialog(
                        title = it.data!!.message,
                        textPositive = "Có",
                        positiveListener = {
                            val bundel = bundleOf(
                                "mode" to VerifyOtpFragment.VERIFY_RESET_PASSWORD_MODE,
                                "phone" to binding.edtPhone.text.toString(),
                                "password" to binding.edtPhone.text.toString()
                            )
                            findNavController().navigate(
                                R.id.action_forgotPasswordFragment_to_verify,
                                bundel
                            )
                        }
                    )
                }
                Status.ERROR -> {
                    viewModel.hideLoading()
                }
            }
        })
    }
}