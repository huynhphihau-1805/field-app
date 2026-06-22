package com.crayon.fieldapp.ui.screen.monitor.employee.listGroupMember

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.model.GroupUser
import com.crayon.fieldapp.databinding.FragmentListGroupEmployeeBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.GroupUserAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListGroupEmployeeFragment :
    BaseFragment<FragmentListGroupEmployeeBinding, ListGroupEmployeeViewModel>() {

    override val layoutId: Int = R.layout.fragment_list_group_employee

    override val viewModel: ListGroupEmployeeViewModel by viewModel()

    private lateinit var agencyId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = GroupUserAdapter(
            itemClickListener = { toUserDetail(it) }
        )

        binding.rvGroup.apply {
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

    private fun toUserDetail(groupUser: GroupUser) {
        val bundel = bundleOf(
            "groupId" to groupUser.id,
            "agencyId" to agencyId,
            "groupName" to groupUser.name
        )
        findNavController().navigate(
            R.id.action_manageEmployeeFragment_to_detailGroupUserFragment,
            bundel
        )
    }
}