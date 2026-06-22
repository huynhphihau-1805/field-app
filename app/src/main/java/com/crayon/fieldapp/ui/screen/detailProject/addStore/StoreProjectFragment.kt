package com.crayon.fieldapp.ui.screen.detailProject.addStore

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentStoreProjectBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailProject.addStore.adapter.AddStoreRVAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class StoreProjectFragment : BaseFragment<FragmentStoreProjectBinding, StoreProjectViewModel>() {

    override val layoutId: Int = R.layout.fragment_store_project

    override val viewModel: StoreProjectViewModel by viewModel()


    private lateinit var agencyId: String
    private lateinit var projectId: String
    private var adapterStores: AddStoreRVAdapter? = null

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
            adapterStores?.let {
                viewModel.addStoreToProject(
                    agencyId,
                    projectId,
                    it.getSelectedItem() as ArrayList<String>
                )
            }
        }

        binding.cbSelectAll.setOnCheckedChangeListener { compoundButton, isChecked ->
            adapterStores?.selectAllItems(isChecked)
        }

        adapterStores = AddStoreRVAdapter(
            arrayListOf(),
            context = requireContext(),
            itemClickListener = { toStoreDetail(it.id) }
        )

        binding.rvStores.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapterStores
        }

        viewModel.apply {
            getStore(agencyId)

            myStores.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            adapterStores?.addStore(it)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            })

            isAddStoreSuccess.observe(viewLifecycleOwner, Observer {
                if (it) {
                    requireContext().showDialog(
                        message = "Đã thêm cửa hàng vào dự án",
                        title = "Thông báo",
                        positiveListener = {
                            findNavController().navigateUp()
                        }
                    )
                }
            })

        }
    }

    private fun toStoreDetail(id: String) {
        val bundel = bundleOf("storeId" to id, "agencyId" to agencyId)
        findNavController().navigate(R.id.action_storeProject_to_detailStore, bundel)
    }
}