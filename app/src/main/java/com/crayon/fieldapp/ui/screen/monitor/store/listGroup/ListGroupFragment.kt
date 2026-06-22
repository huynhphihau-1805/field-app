package com.crayon.fieldapp.ui.screen.monitor.store.listGroup

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentListGroupBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.GroupStoreAdapter
import com.crayon.fieldapp.utils.showConfirmDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListGroupFragment : BaseFragment<FragmentListGroupBinding, ListGroupViewModel>() {

    override val layoutId: Int = R.layout.fragment_list_group

    override val viewModel: ListGroupViewModel by viewModel()

    private lateinit var agencyId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = GroupStoreAdapter(
            itemClickListener = { toGroupDetail(it.id.toString()) },
            itemRemoveClickListener = {
                requireContext().showConfirmDialog(
                    title = "Bạn có muốn xoá nhóm này không?",
                    textPositive = "Có",
                    textNegative = "Không",
                    positiveListener = {
                        viewModel.removeGroup(agencyId, it.id.toString())
                    }
                )
            }
        )

        binding.rvGroup.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.apply {
            groups.observe(viewLifecycleOwner, Observer {
                adapter.submitList(it.data)
            })
            getGroups(agencyId)
        }

    }

    private fun toGroupDetail(id: String) {
        val bundel = bundleOf("groupId" to id, "agencyId" to agencyId)
        findNavController().navigate(R.id.action_manageStoreFragment_to_detailGroupFragment, bundel)
    }
}