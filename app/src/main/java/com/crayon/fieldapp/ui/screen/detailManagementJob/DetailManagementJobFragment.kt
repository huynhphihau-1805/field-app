package com.crayon.fieldapp.ui.screen.detailManagementJob

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentDetailManagementJobBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.ManagementTaskAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.formatHourAndDate
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showConfirmDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailManagementJobFragment :
    BaseFragment<FragmentDetailManagementJobBinding, DetailManagementJobViewModel>() {

    override val layoutId: Int = R.layout.fragment_detail_management_job

    override val viewModel: DetailManagementJobViewModel by viewModel()

    private lateinit var jobId: String
    private lateinit var agencyId: String
    private lateinit var projectId: String
    private lateinit var storeId: String
    private lateinit var picId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        jobId = requireArguments().getString("jobId").toString()
        agencyId = requireArguments().getString("agencyId").toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapterTask = ManagementTaskAdapter(
            itemClickListener = { toTaskDetail(it.toString()) },
            itemRemoveClickListener = {
                requireContext().showConfirmDialog(
                    title = "Bạn có muốn xoá nhiệm vụ này không?",
                    textPositive = "Có",
                    textNegative = "Không",
                    positiveListener = {
                        viewModel.removeTask(agencyId, it.id.toString())
                    }
                )
            }
        )

        binding.rvTask.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapterTask
        }

        viewModel.apply {
            getDetailJob(agencyId, jobId)
            getListTask(agencyId, jobId)

            job.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
//                        showLoading()
                    }

                    Status.SUCCESS -> {
//                        hideLoading()
                        it.data?.let { job ->
                            job.project?.let { project ->
                                projectId = project.id.toString()
                                project.name?.let {
                                    binding.txtProjectName.text = it
                                }
                                project.status?.let {
                                    binding.txtStatus.text = it
                                }
                            }
                            job.agency?.name?.let {
                                binding.txtAgencyName.text = it
                            }
                            job.startTime?.let {
                                binding.txtStartDate.text = formatHourAndDate(it)
                            }
                            job.endTime?.let {
                                binding.txtEndTime.text = formatHourAndDate(it)
                            }
                            job.store?.let { store ->
                                storeId = store.id.toString()
                            }
                            job.pic?.let { pic ->
                                picId = pic.id.toString()
                            }
                        }
                    }

                    Status.ERROR -> {
//                        hideLoading()
                    }
                }
            })

            tasks.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            adapterTask.submitList(it)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }

            })

            isRemoveTask.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        getListTask(agencyId, jobId)
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }

            })
        }

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.fabCreate.setSingleClick {
            val bundel = bundleOf(
                "projectId" to projectId,
                "agencyId" to agencyId,
                "storeId" to storeId,
                "jobId" to jobId,
                "picId" to picId
            )
            findNavController().navigate(R.id.to_addTask, bundel)
        }
    }


    private fun toTaskDetail(id: String) {
//        val bundel = bundleOf("taskId" to id)
//        findNavController().navigate(R.id.to_detail_task, bundel)
    }
}