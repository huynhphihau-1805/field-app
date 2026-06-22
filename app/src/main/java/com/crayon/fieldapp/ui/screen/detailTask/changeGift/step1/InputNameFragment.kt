package com.crayon.fieldapp.ui.screen.detailTask.changeGift.step1

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.CustomerResponse
import com.crayon.fieldapp.databinding.FragmentInputNameBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.Utils
import com.crayon.fieldapp.utils.setSingleClick
import org.koin.androidx.viewmodel.ext.android.viewModel

class InputNameFragment(val onNextClick: (customer: CustomerResponse) -> Unit = {}) :
    BaseFragment<FragmentInputNameBinding, InputNameViewModel>() {

    override val layoutId: Int = R.layout.fragment_input_name
    override val viewModel: InputNameViewModel by viewModel()
    private var taskId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskId = requireArguments().getString("taskId").toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNext.setSingleClick {
            var name = binding.edtName.text.toString().trim()
            var phone = binding.edtPhone.text.toString().trim()
            if (name.isNullOrBlank()) {
                binding.edtName.setError("Họ và tên không được để trống")
                return@setSingleClick
            }

            if (phone.isNullOrBlank()) {
                binding.edtPhone.setError("Số điện thoại không được để trống")
                return@setSingleClick
            }

            if (phone.length < 10) {
                binding.edtPhone.setError("Số điện thoại không đúng")
                return@setSingleClick
            }
            Utils.hideKeyboard(requireActivity())
            taskId?.let {
                viewModel.registerCustomer(it, name = name, phone = phone)
            }
        }

        viewModel.createCustomer.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            onNextClick.invoke(it)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })
    }
}