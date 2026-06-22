package com.crayon.fieldapp.ui.screen.detailTask.changeGift

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.CustomerResponse
import com.crayon.fieldapp.data.remote.response.JobResponse
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.dialog.TimeKeepingDialog
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.adapter.CustomerRVAdapter
import com.crayon.fieldapp.utils.PopupMenu
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.formatStartEndFullDate
import com.crayon.fieldapp.utils.setSingleClick
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangeGiftFragment :
    BaseFragment<com.crayon.fieldapp.databinding.FragmentChangeGiftBinding, ChangeGiftViewModel>() {

    override val layoutId: Int = R.layout.fragment_change_gift
    override val viewModel: ChangeGiftViewModel by viewModel()
    private lateinit var mCustomerAdapter: CustomerRVAdapter
    private var taskId: String? = null
    private var jobJson: String? = null
    private var jobResponse: JobResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskId = requireArguments().getString("taskId").toString()
        jobJson = requireArguments().getString("job").toString()
        jobJson?.let {
            jobResponse = Gson().fromJson(it, JobResponse::class.java)
        }
        taskId?.let {
            viewModel.getDetailTask(it)
        }

        mCustomerAdapter = CustomerRVAdapter(
            arrayListOf(), requireContext(),
            true, {

            }, {
                // Item
                val bundle = bundleOf(
                    "isEdit" to true,
                    "taskId" to taskId.toString(),
                    "projectId" to jobResponse?.project?.id.toString(),
                    "customerInfo" to Gson().toJson(it)
                )
                findNavController().navigate(
                    R.id.action_changeGiftFragment_to_detailCustomerFragment,
                    bundle
                )
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rlEmpty.visibility = View.GONE
        binding.rvCustomer.visibility = View.VISIBLE

        jobResponse?.let {
            it.project?.let {
                binding.txtProjectName.text = it.name.toString()
            }
            it.store?.let {
                binding.txtAddress.text = it.address.toString()
            }

            if (it.startTime != null && it.endTime != null) {
                binding.txtTime.text = formatStartEndFullDate(it.startTime!!, it.endTime!!)
            }
            it.status?.let {
                if (it.equals("Processing")) {
                    binding.txtStaus.text = "Đang chạy"
                    binding.txtStaus.setTextColor(
                        requireContext().resources.getColor(
                            R.color.colorAccent,
                            null
                        )
                    )
                } else {
                    binding.txtStaus.text = "Đã đóng"
                    binding.txtStaus.setTextColor(
                        requireContext().resources.getColor(
                            R.color.colorGray,
                            null
                        )
                    )
                }
            }
        }

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.btnAddCustomer.setSingleClick {
            jobResponse?.store?.let { store ->
                if (viewModel.verifyLocation(store)) {
                    val isVerifyOtp = jobResponse?.project?.isVerifyOtp ?: false
                    val projectId = jobResponse?.project?.id.toString()
                    findNavController().navigate(
                        R.id.action_changeGiftFragment_to_addCustomerFragment,
                        bundleOf(
                            "taskId" to taskId, "isVerifyOtp" to isVerifyOtp,
                            "projectId" to projectId
                        )
                    )
                } else {
                    val dialog = TimeKeepingDialog()
                    val bundle = Bundle()
                    viewModel.currentLocation?.let {
                        bundle.putDouble("current_lat", it.latitude)
                        bundle.putDouble("current_long", it.longitude)

                    }
                    bundle.putDouble("store_lat", store.lat ?: 0.0)
                    bundle.putDouble("store_long", store.lng ?: 0.0)
                    bundle.putString("distant", viewModel.strDistant)
                    dialog.arguments = bundle
                    dialog.show(childFragmentManager, dialog.tag)
                }
            }
        }

        binding.imbIcFilter.setSingleClick {
            PopupMenu.showPopupMenuDashboard(requireView(), object : MenuCallback {
                override fun onImport() {
                    findNavController().navigate(
                        R.id.action_changeGiftFragment_to_importGiftFragment,
                        bundleOf(
                            "taskId" to taskId,
                            "projectId" to jobResponse?.project?.id.toString()
                        )
                    )
                }

                override fun onExport() {
                    findNavController().navigate(
                        R.id.action_changeGiftFragment_to_exportGiftFragment,
                        bundleOf(
                            "taskId" to taskId,
                            "projectId" to jobResponse?.project?.id.toString()
                        )
                    )
                }

            })
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("isNew")
            ?.observe(viewLifecycleOwner, Observer { isNew ->
                if (isNew) {
                    taskId?.let {
                        viewModel.getDetailTask(it)
                    }
                }
            })

        binding.rvCustomer.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = mCustomerAdapter
        }

        viewModel.task.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {

                            if (it.customerBills.size == 0) {
                                binding.txtNumCustomer.text = "0 khách hàng"
                                binding.rlEmpty.visibility = View.VISIBLE
                                binding.rvCustomer.visibility = View.GONE
                            } else {
                                binding.txtNumCustomer.text =
                                    it.customerBills.size.toString() + " khách hàng"
                                binding.rlEmpty.visibility = View.GONE
                                binding.rvCustomer.visibility = View.VISIBLE
                                mCustomerAdapter.addAll(it.customerBills.map {
                                    CustomerResponse(
                                        id = it.customer!!.id.toString(),
                                        name = it.customer!!.name.toString(),
                                        mobileNumber = it.customer!!.mobileNumber.toString(),
                                        createdAt = it.createdAt,
                                        updatedAt = it.updatedAt,
                                        customerBill = it.id
                                    )
                                } as ArrayList<CustomerResponse>)
                            }
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