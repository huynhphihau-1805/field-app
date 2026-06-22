package com.crayon.fieldapp.ui.screen.detailTask.endShift

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.databinding.FragmentReportEndshiftBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailTask.base.DetailTaskViewModel
import com.crayon.fieldapp.ui.screen.detailTask.endShift.adapter.ReportEndShiftAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showMessageDialog
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReportEndShiftFragment :
    BaseFragment<FragmentReportEndshiftBinding, DetailTaskViewModel>() {

    override val layoutId: Int = R.layout.fragment_report_endshift
    override val viewModel: DetailTaskViewModel by viewModel()
    private lateinit var taskId: String
    private var productAdapter: ReportEndShiftAdapter? = null
    private var taskResponse: TaskResponse = TaskResponse()
    private var mProducts = ArrayList<TaskResponse.Product>()
    private var mFeedbacks = ArrayList<TaskResponse.Feedback>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskId = requireArguments().getString("taskId").toString()

        binding.imbIcBack.setSingleClick {
            findNavController().popBackStack()
        }
        context?.let {
            productAdapter = ReportEndShiftAdapter(arrayListOf(), it)
            binding.rvProduct.setLayoutManager(LinearLayoutManager(it))
            binding.rvProduct.adapter = productAdapter
        }
        viewModel.getDetailTask(taskId)
        viewModel.task.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            binding.tvTitle.text = it.type.name
                            taskResponse = it
                            mProducts.clear()
                            mFeedbacks.clear()
                            taskResponse.products.forEach {
                                val item = TaskResponse.Product(
                                    name = it.name,
                                    quantity = it.quantity,
                                    id = it.id
                                )
                                mProducts.add(item)

                            }

                            taskResponse.feedbacks.forEach {
                                val item = TaskResponse.Feedback(
                                    name = it.name,
                                    description = it.description,
                                    id = it.id
                                )
                                mFeedbacks.add(item)
                            }
                            val items = ArrayList<Any>()
                            items.addAll(it.products)
                            items.addAll(it.feedbacks)
                            productAdapter?.addProducts(items)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }

        })

        viewModel.updateProduct.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.LOADING -> {
                    binding.pbLoading.visibility = View.VISIBLE
                }

                Status.SUCCESS -> {
                    binding.pbLoading.visibility = View.GONE
                    context?.showMessageDialog(message = it.data!!.message) {
                        findNavController().popBackStack()
                    }
                }

                Status.ERROR -> {
                    binding.pbLoading.visibility = View.GONE
                }
            }
        })

        binding.imbIcFilter.setSingleClick {
            productAdapter?.let {
                val localProducts = it.getProducts() as ArrayList<TaskResponse.Product>
                val localFeedback = it.getFeedbacks() as ArrayList<TaskResponse.Feedback>

                mProducts.forEach {
                    val item = localProducts.first { local ->
                        local.id.equals(it.id)
                    }
                    if (item != null && item.quantity != 0) {
                        it.quantity = it.quantity + item.quantity
                    }
                }
                mFeedbacks.forEach {
                    val item = localFeedback.first { local ->
                        local.id.equals(it.id)
                    }
                    if (item != null && !item.description.isNullOrBlank()) {
                        if (it.description.isNullOrBlank()) {
                            it.description = item.description
                        } else {
                            it.description = it.description + ";" + item.description
                        }

                    }
                }

                viewModel.updateProductFeedback(
                    taskResponse,
                    mProducts as ArrayList<TaskResponse.Product>,
                    mFeedbacks as ArrayList<TaskResponse.Feedback>
                )
            }

        }

        binding.imbAttachment.setSingleClick {
            val taskString = Gson().toJson(taskResponse).toString()
            val bundle = bundleOf("task" to taskString)
            findNavController().navigate(
                R.id.action_reportEndShiftFragment_to_detailAttachmentFragment,
                bundle
            )
        }
    }
}