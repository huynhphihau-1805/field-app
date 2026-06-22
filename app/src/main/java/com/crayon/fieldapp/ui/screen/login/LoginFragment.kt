package com.crayon.fieldapp.ui.screen.login

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentLoginBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.verifyOtp.VerifyOtpFragment
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showMessageDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragment<FragmentLoginBinding, LoginViewModel>() {

    override val layoutId: Int = R.layout.fragment_login

    override val viewModel: LoginViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bnLogin.setOnClickListener {
            viewModel.clickLogin(binding.edtPhone.text.toString(), binding.edtPass.text.toString())
        }

        binding.bnForgot.setSingleClick {
            findNavController().navigate(R.id.action_login_to_forgotPasswordFragment)
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
                            val bundel = bundleOf(
                                "mode" to VerifyOtpFragment.VERIFY_LOGIN_MODE,
                                "phone" to binding.edtPhone.text.toString(),
                                "password" to binding.edtPass.text.toString()
                            )
                            findNavController().navigate(R.id.to_verify, bundel)
                        }

                        Status.ERROR -> {
                            dismissLoadingDialog()
                        }
                    }
                }
            })
            phone.observe(viewLifecycleOwner, Observer {
                context?.showMessageDialog(it)
            })

            password.observe(viewLifecycleOwner, Observer {
                context?.showMessageDialog(it)
            })
        }
    }
}