package com.crayon.fieldapp.ui.screen.detailProject

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.StoreOfProjectResponse
import com.crayon.fieldapp.databinding.FragmentDetailProjectBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.ManageStoreAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.formatDate
import com.crayon.fieldapp.utils.setSingleClick
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailProjectFragment : BaseFragment<FragmentDetailProjectBinding, DetailProjectViewModel>() {

    override val layoutId: Int = R.layout.fragment_detail_project

    override val viewModel: DetailProjectViewModel by viewModel()

    private lateinit var projectId: String
    private lateinit var agencyId: String
    private var adapterStore: ManageStoreAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectId = requireArguments().getString("projectId").toString()
        agencyId = requireArguments().getString("agencyId").toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.fabAddEmployee.setSingleClick {
            val bundel = bundleOf("agencyId" to agencyId, "projectId" to projectId)
            findNavController().navigate(R.id.to_addMember_project, bundel)
        }

        binding.fabAddStore.setSingleClick {
            val bundel = bundleOf("agencyId" to agencyId, "projectId" to projectId)
            findNavController().navigate(R.id.to_store_project, bundel)
        }

        binding.rlMember.setSingleClick {
            val bundel = bundleOf("projectId" to projectId, "agencyId" to agencyId)
            findNavController().navigate(R.id.to_member_project, bundel)
        }

        adapterStore = ManageStoreAdapter(
            arrayListOf(),
            context = requireContext(),
            removeItemClickListener = {
                viewModel.removeStoreToProject(agencyId, projectId, arrayListOf(it.id.toString()))
            },
            itemClickListener = { toManageJobDetail(it) }
        )

        binding.rvStore.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapterStore
        }

        viewModel.apply {
            getProject(agencyId, projectId)
            getMemberProject(agencyId, projectId)
            getStoreProject(agencyId, projectId)

            stores.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            adapterStore?.addStore(it as ArrayList<StoreOfProjectResponse>)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }

            })

            project.observe(viewLifecycleOwner, Observer { projectInfo ->
                when (projectInfo.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        projectInfo.data?.let {
                            it.status?.let {
                                binding.txtStatus.text = it
                            }
                            it.owner?.profile?.let {
                                binding.txtCreator.text = it
                            }
                            it.agency?.name.let {
                                binding.txtAgencyName.text = it
                            }
                            it.brandName?.let {
                                binding.txtBrand.text = it
                            }
                            it.industry?.let {
                                binding.txtIndustry.text = it
                            }
                            it.startDate?.let {
                                binding.txtStartDate.text = formatDate(it)
                            }
                            it.endDate?.let {
                                binding.txtEndTime.text = formatDate(it)
                            }
                            it.name?.let {
                                binding.tvTitle.text = it
                            }
                        }
                    }
                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            })

            members.observe(viewLifecycleOwner, Observer {
                it.data?.let {
                    binding.txtMember.text = it.size.toString()
                }
            })

            isRemoveStoreSuccess.observe(viewLifecycleOwner, Observer { ids ->
                adapterStore?.apply {
                    removeStores(ids)
                }
            })

        }
    }


    private fun toManageJobDetail(store: StoreOfProjectResponse) {
        val bundel = bundleOf(
            "storeId" to store.id,
            "storeName" to store.name,
            "agencyId" to agencyId,
            "projectId" to projectId
        )
        findNavController().navigate(R.id.to_manageJob_project, bundel)
    }
}