package com.crayon.fieldapp.ui.screen.detailTask.changeGift.addCustomer

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentAddCustomerBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.BaseVPAdapter
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.ChangeGiftViewModel
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.step1.InputNameFragment
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.step2.VerifyOtpStep2Fragment
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.step3.InputBillFragment
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.step4.SelectPromotionFragment
import com.crayon.fieldapp.utils.setSingleClick
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddCustomerFragment : BaseFragment<FragmentAddCustomerBinding, ChangeGiftViewModel>() {

    override val layoutId: Int = R.layout.fragment_add_customer
    override val viewModel: ChangeGiftViewModel by viewModel()
    lateinit var mViewPagerAdapter: BaseVPAdapter
    private var _taskId: String? = null
    private var _projectId: String? = null
    private var _customerId: String? = null
    private var _customerPhone: String? = null
    private var _billId: String? = null
    private var _isVerifyOtp: Boolean = false

    private var _inputNameFragment: InputNameFragment? = null
    private var _verifyOtpFragment: VerifyOtpStep2Fragment? = null
    private var _inputBillFragment: InputBillFragment? = null
    private var _selectPromotionFragment: SelectPromotionFragment = SelectPromotionFragment({
        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            "isNew",
            true
        )
        findNavController().navigateUp()
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _taskId = requireArguments().getString("taskId").toString()
        _projectId = requireArguments().getString("projectId").toString()
        _isVerifyOtp = requireArguments().getBoolean("isVerifyOtp", false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        if (_isVerifyOtp) {
            binding.stepperIndicator.setViewPager(binding.pagger, 4)
            binding.stepperIndicator.setLabels(resources.getStringArray(R.array.stepLabels))
        } else {
            binding.stepperIndicator.setViewPager(binding.pagger, 3)
            binding.stepperIndicator.setLabels(resources.getStringArray(R.array.stepLabelsWithoutOTP))
        }
        setupTablayout()

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }
    }


    private fun setupViewPager() {
        mViewPagerAdapter = BaseVPAdapter(childFragmentManager)
        _inputNameFragment = InputNameFragment({ customer ->
            _customerId = customer.id.toString()
            _customerPhone = customer.mobileNumber.toString()
            binding.pagger.setCurrentItem(1, true)
//            stepper_indicator?.currentStep = 1
            _verifyOtpFragment?.setCustomerPhone(_customerPhone.toString())
            _inputBillFragment?.setCustomerId(_customerId.toString())
        })
        _inputNameFragment?.arguments = bundleOf("taskId" to _taskId)

        _verifyOtpFragment = VerifyOtpStep2Fragment({ ->
            binding.pagger.setCurrentItem(2, true)
        })
        _verifyOtpFragment?.arguments = bundleOf(
            "taskId" to _taskId
        )
        _inputBillFragment = InputBillFragment({
            _billId = it.id
            if (_isVerifyOtp) {
                binding.pagger.setCurrentItem(3, true)
            } else {
                binding.pagger.setCurrentItem(2, true)
            }
            _selectPromotionFragment?.setBillId(_billId.toString())

        })
        _inputBillFragment?.arguments = bundleOf(
            "taskId" to _taskId
        )

        _selectPromotionFragment?.arguments = bundleOf(
            "taskId" to _taskId,
            "projectId" to _projectId
        )


        mViewPagerAdapter.addFragment(_inputNameFragment!!, "")
        if (_isVerifyOtp) {
            mViewPagerAdapter.addFragment(_verifyOtpFragment!!, "")
        }
        mViewPagerAdapter.addFragment(_inputBillFragment!!, "")
        mViewPagerAdapter.addFragment(_selectPromotionFragment!!, "")

        binding.pagger.apply {
            offscreenPageLimit = 1
            adapter = mViewPagerAdapter
        }
    }

    private fun setupTablayout() {
        binding.stepperIndicator.addOnStepClickListener {
            binding.pagger.setCurrentItem(it, true)
        }
    }
}