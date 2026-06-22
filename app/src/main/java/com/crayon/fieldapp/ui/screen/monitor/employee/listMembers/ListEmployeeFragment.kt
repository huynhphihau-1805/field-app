package com.crayon.fieldapp.ui.screen.monitor.employee.listMembers

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentListEmployeeBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.UserAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListEmployeeFragment : BaseFragment<FragmentListEmployeeBinding, ListEmployeeViewModel>() {

    override val layoutId: Int = R.layout.fragment_list_employee

    override val viewModel: ListEmployeeViewModel by viewModel()

    private lateinit var agencyId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = UserAdapter(
            itemClickListener = { toUserDetail(it.id) }
        )

        binding.rvEmployee.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.apply {
            users.observe(viewLifecycleOwner, Observer {
                adapter.submitList(it.data)
            })
            getMembers(agencyId)
        }

    }

    private fun toUserDetail(id: String) {
        val bundel = bundleOf("userId" to id)
        findNavController().navigate(
            R.id.action_mangagEmployeFragment_to_DetailUserFragment,
            bundel
        )
    }
}