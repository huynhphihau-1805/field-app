package com.crayon.fieldapp.ui.screen.detailProject.createJob

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.request.CreateJobForm
import com.crayon.fieldapp.databinding.FragmentCreateJobBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailProject.createJob.adapter.SelectMembersRVAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showDialog
import com.crayon.fieldapp.utils.toTimeString
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar
import java.util.Locale

class CreateJobFragment : BaseFragment<FragmentCreateJobBinding, CreateJobViewModel>() {

    override val layoutId: Int = R.layout.fragment_create_job

    override val viewModel: CreateJobViewModel by viewModel()

    private var start_date: String? = null
    private var end_date: String? = null
    private var apdaterMember: SelectMembersRVAdapter? = null
    private lateinit var agencyId: String
    private lateinit var projectId: String
    private lateinit var storeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        agencyId = requireArguments().get("agencyId").toString()
        projectId = requireArguments().get("projectId").toString()
        storeId = requireArguments().get("storeId").toString()
        apdaterMember = SelectMembersRVAdapter(
            arrayListOf(),
            requireContext(),
            itemClickListener = { toUserDetail(it.id) }
        )

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.txtStartDate.setSingleClick {
            selectTime(true)
        }

        binding.txtEndDate.setSingleClick {
            selectTime(false)
        }

        binding.imbIcFilter.setSingleClick {
            val pic = apdaterMember?.getSelectedMembers()
            val form = CreateJobForm(
                agencyId = agencyId,
                projectId = projectId,
                shift_start = start_date,
                shift_end = end_date,
                storeId = storeId,
                picId = pic,
                role = binding.spRole.selectedItem.toString()
            )
            form.validate().also { result ->
                if (result.first) {
                    viewModel.createJob(form)
                } else {
                    handleErrorMessage(result.second)
                }
            }
        }

        binding.rvMembers.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = apdaterMember
        }


        viewModel.apply {
            getMemberProject(agencyId, projectId)

            members.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            apdaterMember?.addAll(it)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            })

            createJob.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                when (it.status) {
                    Status.LOADING -> {
                        showLoading()
                    }

                    Status.SUCCESS -> {
                        hideLoading()
                        requireContext().showDialog(
                            message = "Đã tạo công việc",
                            title = "Thông báo",
                            positiveListener = {
                                findNavController().navigateUp()
                            }
                        )
                    }

                    Status.ERROR -> {
                        hideLoading()
                    }
                }
            })
        }
    }

    private fun selectTime(isStartTime: Boolean) {
        val c = Calendar.getInstance()
        var mYear = 0
        var mMonth = 0
        var mDay = 0
        var mHour = 0
        var mMintues = 0

        DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                mYear = year
                mMonth = monthOfYear
                mDay = dayOfMonth

                TimePickerDialog(
                    requireContext(),
                    TimePickerDialog.OnTimeSetListener() { view, hourOfDay, minuteOfHour ->
                        mHour = hourOfDay
                        mMintues = minuteOfHour
                        val time = formatDate(mYear, mMonth, mDay, mHour, mMintues)
                        if (isStartTime) {
                            start_date =
                                time.toTimeString("yyyy-MM-dd'T'HH:mm:ss Z", Locale.getDefault())
                            start_date?.let {
                                binding.txtStartDate.text =
                                    time.toTimeString("dd/MM/yyyy HH:mm", Locale.getDefault())
                            }
                        } else {
                            end_date =
                                time.toTimeString("yyyy-MM-dd'T'HH:mm:ss Z", Locale.getDefault())
                            end_date?.let {
                                binding.txtEndDate.text =
                                    time.toTimeString("dd/MM/yyyy HH:mm", Locale.getDefault())
                            }
                        }
                    },
                    0,
                    0,
                    false
                ).show()
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()

    }

    private fun toUserDetail(id: String) {
        val bundel = bundleOf("userId" to id)
        findNavController().navigate(R.id.action_AddJobProject_to_detailUser, bundel)
    }

    private fun formatDate(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        return calendar.timeInMillis
    }

}