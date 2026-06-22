package com.crayon.fieldapp.ui.screen.detailAttendance

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.databinding.FragmentDetailAttendanceBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailAttendanceFragment :
    BaseFragment<FragmentDetailAttendanceBinding, DetailAttendanceViewModel>() {

    override val layoutId: Int = R.layout.fragment_detail_attendance

    override val viewModel: DetailAttendanceViewModel by viewModel()

    lateinit var agencyId: String
    lateinit var projectId: String
    lateinit var storeId: String
    lateinit var startTime: String
    lateinit var endTime: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
        projectId = requireArguments().get("projectId").toString()
        storeId = requireArguments().get("storeId").toString()
        startTime = requireArguments().get("startTime").toString()
        endTime = requireArguments().get("endTime").toString()

        viewModel.getManagementAttendances(
            agencyId = agencyId,
            projectId = projectId,
            storeId = storeId,
            startTime = startTime,
            endTime = endTime
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        viewModel.apply {
            attendances.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let { tasks ->
                            if (tasks.size == 0) {
                                binding.txtMember.text = "0 nhân viên"
                            } else {
                                binding.tvTitle.text = tasks[0].store!!.name
                                binding.txtMember.text =
                                    tasks.distinctBy { it.pic?.id }.size.toString() + " nhân viên"

                                tasks?.let {
                                    var numDidTimeKeeping =
                                        viewModel.getNumDidTimeKeeping(it as ArrayList<TaskResponse>)
                                    var numLate =
                                        viewModel.getNumLateTimeKeeping(it as ArrayList<TaskResponse>)
                                    var numEarl =
                                        viewModel.getNumEarlTimeKeeping(it as ArrayList<TaskResponse>)
                                    binding.txtDidTimekeeping.text = numDidTimeKeeping.toString()
                                    binding.txtNotYetTimekeeping.text =
                                        (tasks.size - numDidTimeKeeping).toString()
                                    binding.txtLate.text = numLate.toString()
                                    binding.txtEarly.text = numEarl.toString()
                                }
                            }
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            })
        }
    }
}