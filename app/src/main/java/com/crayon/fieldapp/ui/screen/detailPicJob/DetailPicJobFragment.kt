package com.crayon.fieldapp.ui.screen.detailPicJob

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.data.remote.response.TaskType
import com.crayon.fieldapp.databinding.FragmentDetailPicJobBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailPicJob.adapter.ListTaskAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.formatDate
import com.crayon.fieldapp.utils.formatHourAndDate
import com.crayon.fieldapp.utils.setSingleClick
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailPicJobFragment : BaseFragment<FragmentDetailPicJobBinding, DetailPicJobViewModel>() {

    override val layoutId: Int = R.layout.fragment_detail_pic_job
    override val viewModel: DetailPicJobViewModel by viewModel()

    private lateinit var jobId: String
    private var jobResponse: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        jobId = requireArguments().getString("jobId").toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.rvTask.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val adapterTask = ListTaskAdapter(
            arrayListOf(), requireContext(), {
                toTaskDetail(it.id.toString(), it.type.identifier)
            })

        binding.rvTask.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapterTask
        }

        viewModel.apply {
            getDetailJob(jobId)
            getListTask(jobId)

            tasks.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            adapterTask.addAll(it as ArrayList<TaskResponse>)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }

            })

            job.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            jobResponse = Gson().toJson(it)
                            it.project?.name?.let {
                                binding.txtProjectName.text = it
                            }
                            it.agency?.name?.let {
                                binding.txtAgencyName.text = it
                            }
                            it.status?.let {
                                binding.txtStatus.text = it
                            }
                            it.startTime?.let {
                                binding.txtStartDate.text = formatHourAndDate(it)
                            }
                            it.endTime?.let {
                                binding.txtEndTime.text = formatHourAndDate(it)
                            }
                        }
                    }

                    Status.ERROR -> {
                    }
                }

            })
        }

    }

    private fun toTaskDetail(taskId: String, type: Int) {
        if (type == TaskType.REPORT_END_SHIFT.value) {
            val bundel = bundleOf("taskId" to taskId)
            findNavController().navigate(
                R.id.action_detailPicJobFragment_to_reportEndShiftFragment,
                bundel
            )
        } else if (type == TaskType.TIME_KEEPING.value) {
            val bundel = bundleOf("taskId" to taskId)
            findNavController().navigate(
                R.id.action_detailPicJobFragment_to_reportAttendanceFragment,
                bundel
            )
        } else if (type == TaskType.UPDATE_STATUS.value) {
            val bundel = bundleOf("taskId" to taskId)
            findNavController().navigate(
                R.id.action_detailPicJobFragment_to_reportUpdateStatusFragment,
                bundel
            )
        } else if (type == TaskType.CHANGE_GIFT.value) {
            val bundel = bundleOf("taskId" to taskId, "job" to jobResponse.toString())
            findNavController().navigate(
                R.id.action_detailPicJobFragment_to_changeGiftFragment,
                bundel
            )
        } else if (type == TaskType.REPORT_COMPITETOR.value) {
            val bundel = bundleOf("taskId" to taskId, "job" to jobResponse.toString())
            findNavController().navigate(
                R.id.action_detailPicJobFragment_to_reportCompetitorFragment,
                bundel
            )
        } else if (type == TaskType.REPORT_SALES.value) {
            val bundel = bundleOf("taskId" to taskId, "job" to jobResponse.toString())
            findNavController().navigate(
                R.id.action_detailPicJobFragment_to_reportSalesFragment,
                bundel
            )
        } else {
            val bundel = bundleOf("taskId" to taskId)
            findNavController().navigate(R.id.action_detailPicJob_to_detailTask, bundel)
        }

    }
}