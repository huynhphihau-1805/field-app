package com.crayon.fieldapp.ui.screen.bank

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentBankBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.SimpleSPAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showMessageDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class BankFragment : BaseFragment<FragmentBankBinding, BankViewModel>() {

    override val layoutId: Int = R.layout.fragment_bank

    override val viewModel: BankViewModel by viewModel()

    private lateinit var bankAdapter: SimpleSPAdapter
    private var bankCode: String? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bankAdapter = SimpleSPAdapter(this@BankFragment.requireContext(), arrayListOf())
        viewModel.apply {
            getBankInfo()
            getListBank()
        }

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.spBankName.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                bankCode = bankAdapter.getItem(position).name
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.imbIcFilter.setSingleClick {
            viewModel.updateBankInfo(
                tax = binding.edtTax.text.toString().trim(),
                bankNumber = binding.edtBankId.text.toString().trim(),
                bankName = bankAdapter.getItem(binding.spBankName.selectedItemPosition).name.trim(),
                bankBrand = binding.edtBrand.text.toString().trim()
            )
        }

        viewModel.apply {
            banks.observe(viewLifecycleOwner, Observer {
                bankAdapter.addItems(it)
                binding.spBankName.adapter = bankAdapter
            })

            user.observe(viewLifecycleOwner, Observer {
                var current_bank = bankAdapter.getPositionByName(it.profile!!.bankName.toString())
                if (current_bank != -1) {
                    bankCode = bankAdapter.getItem(current_bank).id
                    binding.spBankName.setSelection(current_bank)
                }
            })

            updateBank.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        showLoading()
                    }

                    Status.SUCCESS -> {
                        hideLoading()
                        requireContext().showMessageDialog(title = "Cập nhật thông tin ngân hàng thành công")
                    }

                    Status.ERROR -> {
                        hideLoading()
                    }
                }
            })

        }
    }
}