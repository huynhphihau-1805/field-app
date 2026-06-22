package com.crayon.fieldapp.ui.screen.monitor.changeGift.detailTask

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.CustomerResponse
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.databinding.FragmentDetailChangeGiftAtStoreBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.adapter.CustomerRVAdapter
import com.crayon.fieldapp.utils.formatStartEndFullDate
import com.crayon.fieldapp.utils.setSingleClick
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailChangeGiftAtStoreFragment() :
    BaseFragment<FragmentDetailChangeGiftAtStoreBinding, DetailChangeGiftAtStoreViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_detail_change_gift_at_store

    private var task: String? = null
    private var taskResponse: TaskResponse? = null
    private var mCompetitorAdapter: CustomerRVAdapter? = null
    private var listCustomer: ArrayList<CustomerResponse> = arrayListOf()

    override val viewModel: DetailChangeGiftAtStoreViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        task = requireArguments().get("task").toString()
        taskResponse = Gson().fromJson(task, TaskResponse::class.java)
        taskResponse?.customerBills?.let { mCustomers ->
            listCustomer.addAll(
                mCustomers.map {
                    CustomerResponse(
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        name = it.customer?.name,
                        mobileNumber = it.customer?.mobileNumber,
                        customerBill = it.id.toString()
                    )
                }
            )
        }

        mCompetitorAdapter =
            CustomerRVAdapter(
                listCustomer, requireContext(), false,
                onItemClick = {
                    val bundle = bundleOf(
                        "isEdit" to false,
                        "taskId" to taskResponse?.id.toString(),
                        "projectId" to taskResponse?.project?.id.toString(),
                        "customerInfo" to Gson().toJson(it)
                    )
                    findNavController().navigate(
                        R.id.action_detail_changeGift_to_detail_customer,
                        bundle
                    )
                })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.imbIcBack.setSingleClick {
            findNavController().popBackStack()
        }

        taskResponse?.let {
            it.project?.let {
                binding.txtProjectName.text = it.name.toString()
            }
            it.store?.let {
                binding.txtAddress.text = it.address.toString()
                binding.tvTitle.text = it.name.toString()
            }

            it.job?.let { job ->
                if (job.startTime != null && job.endTime != null) {
                    binding.txtTime.text = formatStartEndFullDate(job.startTime!!, job.endTime!!)
                }
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

            binding.txtNumCustomer.text = listCustomer.size.toString() + " khách hàng"
        }


        binding.rvCustomer.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = mCompetitorAdapter
        }

    }
}