package com.crayon.fieldapp.ui.screen.application.list

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentListPicApplicationsBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.ApplicationAdapter
import com.crayon.fieldapp.utils.Status
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListPicApplicationFragment :
    BaseFragment<FragmentListPicApplicationsBinding, ListPicApplicationViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_list_pic_applications

    lateinit var agencyId: String
    lateinit var status: String

    override val viewModel: ListPicApplicationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().getString("agencyId", "")
        status = requireArguments().getString("status", "")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ApplicationAdapter()

        binding.rvApplication.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.applications.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    binding.pbLoading.visibility = View.VISIBLE
                }

                Status.SUCCESS -> {
                    binding.pbLoading.visibility = View.GONE
                    resource.data?.let { data ->
                        adapter.submitList(data)
                    }
                }

                Status.ERROR -> {
                    binding.pbLoading.visibility = View.GONE
                }
            }
        }

        if (status.isNotEmpty()) {
            viewModel.getMyApplications(status)
        }
    }
}
