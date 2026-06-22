package com.crayon.fieldapp.ui.screen.monitor.reportSales.detailTask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.OrderResponse
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.databinding.FragmentDetailReportSalesAtStoreBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.monitor.reportSales.detailTask.adapter.ManageOrderRVAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.formatStartEndFullDate
import com.crayon.fieldapp.utils.setSingleClick
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailReportSalesAtStoreFragment() :
    BaseFragment<FragmentDetailReportSalesAtStoreBinding, DetailReportSalesAtStoreViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_detail_report_sales_at_store

    private var task: String? = null
    private var taskResponse: TaskResponse? = null
    private var mOrderAdapter: ManageOrderRVAdapter? = null
    override val viewModel: DetailReportSalesAtStoreViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        task = requireArguments().get("task").toString()
        taskResponse = Gson().fromJson(task, TaskResponse::class.java)

        mOrderAdapter =
            ManageOrderRVAdapter(arrayListOf(), requireContext(), {
                // Item
                val bundel = bundleOf("order" to Gson().toJson(it))
                findNavController().navigate(
                    R.id.action_detail_reportSales_to_detail_order,
                    bundel
                )
            })

        taskResponse?.let {
            viewModel.fetchOrders(it.id.toString())
        }

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
            it.job?.let {
                if (it.startTime != null && it.endTime != null) {
                    binding.txtTime.text = formatStartEndFullDate(it.startTime!!, it.endTime!!)
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
        }
        viewModel.orders.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            binding.txtNumCustomer.text = it.size.toString() + " đơn hàng"
                            if (it.size == 0) {
                                binding.rlEmpty.visibility = View.VISIBLE
                                binding.rvOrder.visibility = View.GONE
                            } else {
                                binding.rlEmpty.visibility = View.GONE
                                binding.rvOrder.visibility = View.VISIBLE
                                mOrderAdapter?.addAll(it as ArrayList<OrderResponse>)
                            }
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })


        binding.rvOrder.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = mOrderAdapter
        }

    }
}