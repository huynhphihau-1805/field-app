package com.crayon.fieldapp.ui.screen.createApplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.model.SimpleModel
import com.crayon.fieldapp.data.remote.request.CreateApplicationForm
import com.crayon.fieldapp.data.remote.response.ApplicationType
import com.crayon.fieldapp.data.remote.response.PicProjectResponse
import com.crayon.fieldapp.databinding.FragmentCreateApplicationBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.SimpleSPAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.formatDate
import com.crayon.fieldapp.utils.formatHour
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showMessageDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateApplicationFragment :
    BaseFragment<FragmentCreateApplicationBinding, CreateApplicationViewModel>() {

    override val layoutId: Int = R.layout.fragment_create_application
    override val viewModel: CreateApplicationViewModel by viewModel()

    lateinit var agencyAdapter: SimpleSPAdapter
    lateinit var projectAdapter: SimpleSPAdapter
    lateinit var replacementAdapter: SimpleSPAdapter
    lateinit var jobAdapter: SimpleSPAdapter
    lateinit var typeApplicationAdapter: SimpleSPAdapter

    var agencyId: String? = null
    var projectId: String? = null
    var leaderId: String? = null
    var replacementId: String? = null
    var jobId: String? = null
    var listProjectResponse = ArrayList<PicProjectResponse>()
    private val calendar: Calendar

    init {
        calendar = Calendar.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.txtDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.imbIcFilter.setOnClickListener {
            val reason = binding.edtReason.text.toString().trim()
            val type = typeApplicationAdapter.getItem(binding.spType.selectedItemPosition).id
            val applicationForm = CreateApplicationForm()
            applicationForm.agency = agencyId
            applicationForm.leader = leaderId
            applicationForm.type = type
            applicationForm.project = projectId
            applicationForm.reason = reason
            applicationForm.start_time = binding.txtDate.text.toString()
            applicationForm.end_time = binding.txtDate.text.toString()
            if (type.equals(ApplicationType.SWITCH_SHIFT.value)) {
                applicationForm.replacement = replacementId
                applicationForm.job = jobId
            }

            applicationForm.validate().also { result ->
                if (result.first) {
                    viewModel.createApplication(applicationForm)
                } else {
                    handleErrorMessage(result.second)
                }
            }
        }

        agencyAdapter = SimpleSPAdapter(requireContext(), arrayListOf())
        binding.spAgency.setAdapter(agencyAdapter)

        projectAdapter = SimpleSPAdapter(requireContext(), arrayListOf())
        binding.spProject.setAdapter(projectAdapter)

        replacementAdapter = SimpleSPAdapter(requireContext(), arrayListOf())
        binding.spReplacement.setAdapter(replacementAdapter)

        jobAdapter = SimpleSPAdapter(requireContext(), arrayListOf())
        binding.spJob.setAdapter(jobAdapter)

        val listApplications = ApplicationType.values().map {
            SimpleModel(it.text, it.value)
        }

        typeApplicationAdapter = SimpleSPAdapter(
            requireContext(),
            listApplications as ArrayList<SimpleModel>
        )
        binding.spType.setAdapter(typeApplicationAdapter)

        viewModel.apply {
            getListRole()
            listAgency.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let { listRole ->
                            val items =
                                listRole.map {
                                    SimpleModel(
                                        it.agency.name.toString(),
                                        it.agency.id
                                    )
                                }
                            agencyAdapter.addItems(items as ArrayList<SimpleModel>)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }

            })

            createApplication.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        context?.showMessageDialog("Tạo đơn thành công") {
                            findNavController().navigateUp()
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            })

            listProject.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let { listProject ->
                            listProjectResponse.addAll(listProject)
                            val items =
                                listProject.map {
                                    SimpleModel(
                                        it.name.toString(),
                                        it.id.toString()
                                    )
                                }
                            projectAdapter.addItems(items as ArrayList<SimpleModel>)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            })

            listMember.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let { listMember ->
                            val items =
                                listMember.map {
                                    SimpleModel(
                                        it.lastName + " " + it.firstName,
                                        it.id.toString()
                                    )
                                }
                            replacementAdapter.addItems(items as ArrayList<SimpleModel>)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            })

            listJob.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let { listJob ->
                            val items =
                                listJob.map {
                                    val start_date = formatHour(it.startTime.toString())
                                    val end_date = formatHour(it.endTime.toString())
                                    val shift =
                                        formatDate(it.startTime.toString()) + " ca " + start_date + "-" + end_date
                                    val name = it.store!!.name + ": " + shift
                                    SimpleModel(
                                        name,
                                        it.id.toString()
                                    )
                                }
                            jobAdapter.addItems(items as ArrayList<SimpleModel>)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            })
        }

        binding.spAgency?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                agencyId = agencyAdapter.getItem(position).id
                viewModel.getProjectByAgency(agencyId.toString())
            }
        }

        binding.spProject.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                projectId = listProjectResponse.get(position).id.toString()
                leaderId = listProjectResponse.get(position).owner?.id.toString()
                projectId?.let {
                    viewModel.getMyJobOfProject(it)
                    viewModel.getMemberOfProject(it)
                }
            }
        }

        binding.spType.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                if (typeApplicationAdapter.getItem(position).id.equals(ApplicationType.SWITCH_SHIFT.value)) {
                    binding.spReplacement.visibility = View.VISIBLE
                    binding.tvReplacement.visibility = View.VISIBLE

                    binding.spJob.visibility = View.VISIBLE
                    binding.tvJob.visibility = View.VISIBLE
                    projectId?.let {
                        viewModel.getMemberOfProject(it)
                        viewModel.getMyJobOfProject(it)
                    }
                } else {
                    binding.spReplacement.visibility = View.GONE
                    binding.tvReplacement.visibility = View.GONE
                    binding.spJob.visibility = View.GONE
                    binding.tvJob.visibility = View.GONE
                }
            }
        }

        binding.spReplacement.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                replacementId = replacementAdapter.getItem(position).id
            }
        }

        binding.spJob.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                jobId = jobAdapter.getItem(position).id
            }
        }
    }


    private fun formatTime() {
        val myFormat = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.txtDate.text = sdf.format(calendar.time).toString()
    }

    private val dateSetListener =
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            formatTime()
        }
}