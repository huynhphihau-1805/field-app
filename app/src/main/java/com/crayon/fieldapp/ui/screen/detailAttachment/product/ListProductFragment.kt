package com.crayon.fieldapp.ui.screen.detailAttachment.product

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentListProductBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailAttachment.product.adapter.ProductAdapter
import com.crayon.fieldapp.ui.screen.detailTask.base.DetailTaskViewModel
import com.crayon.fieldapp.utils.Status
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListProductFragment : BaseFragment<FragmentListProductBinding, DetailTaskViewModel>() {

    override val layoutId: Int = R.layout.fragment_list_product
    override val viewModel: DetailTaskViewModel by viewModel()

    private lateinit var productAdapter: ProductAdapter
    private var taskId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskId = requireArguments().get("task").toString()
        productAdapter = ProductAdapter(arrayListOf(), requireContext())
        viewModel.getDetailTask(taskId.toString())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvProudct.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.rvProudct.setHasFixedSize(true)
        binding.rvProudct.setAdapter(productAdapter)

        viewModel.task.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
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
    }
}