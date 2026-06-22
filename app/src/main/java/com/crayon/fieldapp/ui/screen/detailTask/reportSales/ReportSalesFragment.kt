package com.crayon.fieldapp.ui.screen.detailTask.reportSales

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.JobResponse
import com.crayon.fieldapp.data.remote.response.OrderResponse
import com.crayon.fieldapp.databinding.FragmentReportSalesBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.dialog.TimeKeepingDialog
import com.crayon.fieldapp.ui.screen.detailTask.reportSales.adapter.OrderRVAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.formatStartEndFullDate
import com.crayon.fieldapp.utils.setSingleClick
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReportSalesFragment :
    BaseFragment<FragmentReportSalesBinding, ReportSalesViewModel>() {

    override val layoutId: Int = R.layout.fragment_report_sales
    override val viewModel: ReportSalesViewModel by viewModel()
    private lateinit var mOrderAdatper: OrderRVAdapter
    private lateinit var taskId: String
    private var jobJson: String? = null
    private var updateOrderJson: String? = null
    private var jobResponse: JobResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskId = requireArguments().getString("taskId").toString()
        jobJson = requireArguments().getString("job").toString()
        updateOrderJson = requireArguments().getString("updateOrder").toString()
        jobJson?.let {
            jobResponse = Gson().fromJson(it, JobResponse::class.java)
        }

        mOrderAdatper = OrderRVAdapter(arrayListOf(
        ), requireContext(), onItemClickListener = {
            val order = Gson().toJson(it).toString()
            findNavController().navigate(
                R.id.action_reportSalesFragment_to_detailOrderFragment,
                bundleOf("order" to order)
            )
        }, onItemEditListener = {
            val order = Gson().toJson(it).toString()
            findNavController().navigate(
                R.id.action_reportSalesFragment_to_addOrderFragment,
                bundleOf(
                    "taskId" to taskId,
                    "projectId" to jobResponse?.project?.id.toString(),
                    "order" to order
                )
            )
        }, isEdit = true
        )

        viewModel.fetchOrders(taskId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    findNavController().navigate(
                        R.id.action_reportSalesFragment_to_addOrderFragment,
                        bundleOf(
                            "taskId" to taskId,
                            "projectId" to jobResponse?.project?.id.toString()
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("isNew")
            ?.observe(viewLifecycleOwner, Observer { isNew ->
                if (isNew) {
                    taskId?.let {
                        viewModel.fetchOrders(it)
                    }
                }
            })

        binding.rvCustomer.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = mOrderAdatper
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
                            if (it.size == 0) {
                                binding.txtNumCustomer?.visibility = View.GONE
                                binding.rlEmpty.visibility = View.VISIBLE
                                binding.rvCustomer.visibility = View.GONE
                            } else {
                                binding.txtNumCustomer.visibility = View.VISIBLE
                                binding.txtNumCustomer.text = it.size.toString() + " đơn hàng"
                                binding.rlEmpty.visibility = View.GONE
                                binding.rvCustomer.visibility = View.VISIBLE
                                mOrderAdatper.addAll(it as ArrayList<OrderResponse>)
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