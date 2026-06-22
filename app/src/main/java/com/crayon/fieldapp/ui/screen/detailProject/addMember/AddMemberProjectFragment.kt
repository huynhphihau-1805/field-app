package com.crayon.fieldapp.ui.screen.detailProject.addMember

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentAddMemberProjectBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailProject.addStore.adapter.AddMemberRVAdapter
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddMemberProjectFragment :
    BaseFragment<FragmentAddMemberProjectBinding, AddMemberProjectViewModel>() {

    override val layoutId: Int = R.layout.fragment_add_member_project

    override val viewModel: AddMemberProjectViewModel by viewModel()


    private lateinit var agencyId: String
    private lateinit var projectId: String
    private var adapterMembers: AddMemberRVAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
        projectId = requireArguments().get("projectId").toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.btnChoose.setSingleClick {
            adapterMembers?.let {
                viewModel.addMembersToProject(
                    agencyId,
                    projectId,
                    it.getSelectedItem() as ArrayList<String>
                )
            }
        }

        binding.cbSelectAll.setOnCheckedChangeListener { compoundButton, isChecked ->
            adapterMembers?.selectAllItems(isChecked)
        }


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapterMembers = AddMemberRVAdapter(
            arrayListOf(),
            context = requireContext(),
            itemClickListener = { toUserDetail(it.id) }
        )

        binding.rvMembers.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapterMembers
        }

        viewModel.apply {
            getMembers(agencyId)

            users.observe(viewLifecycleOwner, Observer {
                it.data?.let {
                    adapterMembers?.addUser(it)
                }
            })

            isAddUserSuccess.observe(viewLifecycleOwner, Observer {
                if (it) {
                    requireContext().showDialog(
                        message = "Đã thêm thành viên vào dự án",
                        title = "Thông báo",
                        positiveListener = {
                            findNavController().navigateUp()
                        }
                    )
                }
            })

        }
    }

    private fun toUserDetail(id: String) {
        val bundel = bundleOf("userId" to id)
        findNavController().navigate(R.id.action_addMemberProject_to_detailUser, bundel)
    }
}