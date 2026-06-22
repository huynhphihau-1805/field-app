package com.crayon.fieldapp.ui.screen.report.project

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.model.SimpleModel
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.databinding.FragmentReportProjectBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.SimpleSPAdapter
import com.crayon.fieldapp.ui.screen.report.adapter.SimpleRVAdapter
import com.crayon.fieldapp.ui.screen.report.adapter.TodayShiftRVAdapter
import com.crayon.fieldapp.ui.widgets.MtsCalendarView
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.toTimeString
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.androidx.viewmodel.ext.android.viewModel
import studio.phillip.yolo.utils.TimeFormatUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class ReportByProjectFragment :
    BaseFragment<FragmentReportProjectBinding, ReportProjectViewModel>() {

    override val layoutId: Int = R.layout.fragment_report_project

    override val viewModel: ReportProjectViewModel by viewModel()
    private lateinit var selectProjectAdapter: SimpleSPAdapter
    private var listAllProject: ArrayList<SimpleModel>? = null
    private var selectProjectIds = arrayListOf("all")
    var calendar = Calendar.getInstance()
    private lateinit var selectShift: HashMap<Date, ArrayList<TaskResponse>>
    private var isFistTime: Boolean = true

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("ids")
            ?.observe(viewLifecycleOwner, Observer { result ->

                val itemType = object : TypeToken<List<SimpleModel>>() {}.type
                val ids = Gson().fromJson<List<SimpleModel>>(result, itemType)
                listAllProject = ids as ArrayList<SimpleModel>?

                var data = ArrayList<SimpleModel>()
                var numProjects = "Tất cả " + listAllProject?.size.toString() + " dự án"

                data.add(SimpleModel(numProjects, "all"))
                listAllProject?.forEach {
                    data.add(SimpleModel(it.name, it.id))
                }
                selectProjectAdapter.addItems(data)
                binding.spProject.adapter = selectProjectAdapter

            })

        var summaryShiftAdapter = SimpleRVAdapter(arrayListOf(), requireContext())
        var todayShiftadapter = TodayShiftRVAdapter(arrayListOf(), false, requireContext())

        binding.rvTodayShift.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.rvTodayShift.adapter = todayShiftadapter

        binding.rvSummaryShift.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.rvSummaryShift.adapter = summaryShiftAdapter

        viewModel.setValue()
        binding.btnLoadMore.setOnClickListener {
            viewModel.projects.removeObservers(viewLifecycleOwner)
            findNavController().navigate(R.id.action_report_to_loginFragment)
        }

        selectProjectAdapter = SimpleSPAdapter(requireContext(), arrayListOf())
        binding.spProject.adapter = selectProjectAdapter

        binding.spProject.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                val projectId = selectProjectAdapter.getItem(position).id
                if (projectId.equals("all")) {
                    selectProjectIds = selectProjectAdapter.getIds() as ArrayList<String>
                    viewModel.getReportByProject(calendar)
                } else {
                    selectProjectIds = arrayListOf(projectId)
                    viewModel.getReportByProject(
                        calendar
                    )
                }
            }
        }

        binding.calendarView?.setEventHandler(object : MtsCalendarView.EventHandler {
            override fun onMonthPress(cal: Calendar) {
                calendar = cal
                viewModel.getReportByProject(cal)
            }

            override fun onDayShortPress(date: Date) {
                try {
                    if (selectShift != null) {
                        val selectDate = TimeFormatUtils.getResetDate(date)
                        val arrTask = selectShift.get(selectDate)
                        if (arrTask != null) {
                            var temp = ArrayList<TaskResponse>()
                            temp.addAll(arrTask)
                            todayShiftadapter.addAll(temp)

                            binding.txtSelectDate.visibility = View.VISIBLE
                            binding.txtSelectDate.text = resources.getString(
                                R.string.txt_timekeeping_date,
                                date.toTimeString("dd/MM/yyyy"), arrTask?.size ?: 0
                            )
                        } else {
                            binding.txtSelectDate.visibility = View.GONE
                            todayShiftadapter.clear()
                        }
                    }
                } catch (e: Exception) {

                }
            }

            override fun onDayLongPress(date: Date) {
                val df = SimpleDateFormat.getDateInstance()
                Toast.makeText(
                    requireContext(),
                    df.format(date),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        viewModel.getProject()
        viewModel.projects.observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        if (isFistTime) {
                            isFistTime = false
                            binding.pbLoading.visibility = View.GONE
                            listAllProject = it.data?.map {
                                SimpleModel(
                                    id = it.id.toString(),
                                    name = it.name.toString()
                                )
                            } as ArrayList<SimpleModel>

                            var data = ArrayList<SimpleModel>()
                            var numProjects = "Tất cả " + listAllProject?.size.toString() + " dự án"
                            data.add(SimpleModel(numProjects, "all"))
                            listAllProject?.forEach {
                                data.add(SimpleModel(it.name.toString(), it.id.toString()))
                            }
                            selectProjectAdapter.addItems(data)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }


        })
        viewModel.listTask.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.LOADING -> {
                    binding.pbLoading.visibility = View.VISIBLE
                }

                Status.SUCCESS -> {
                    binding.pbLoading.visibility = View.GONE
                    var tasks = ArrayList<TaskResponse>()
                    if (selectProjectIds.size == 1 && selectProjectIds.get(0).equals("all")) {
                        tasks = it.data!! as ArrayList<TaskResponse>
                    } else {
                        tasks = it.data?.filter {
                            selectProjectIds.contains(it.project?.id.toString())
                        } as ArrayList<TaskResponse>
                    }

                    // Get summary
                    val numberOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val start_date = TimeFormatUtils.getDate(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        1,
                        0,
                        0
                    )!!.toTimeString("dd/MM/yyyy")
                    val end_date = TimeFormatUtils.getDate(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        numberOfMonth,
                        23,
                        59
                    )!!.toTimeString("dd/MM/yyyy")
                    binding.txtSummaryDate.text =
                        "Thông kê từ ngày " + start_date + " đến ngày " + end_date


                    // Get invalid shift
                    val invalidShift =
                        viewModel.calculateTotalInvalidShift(tasks as ArrayList<TaskResponse>)
                    binding.txtSummaryInvalidShift.text =
                        getString(R.string.txt_summary_invalid_shift, invalidShift)

                    // Get total man Hour
                    val manHour = viewModel.calculateTotalManHour(tasks)
                    Log.d(
                        "AAA-project : ",
                        TimeFormatUtils.convertSecondToHour(manHour) + "/" + "man:" + manHour
                    )
                    binding.txtSummaryHour.text = getString(
                        R.string.txt_summary_hour,
                        TimeFormatUtils.convertSecondToHour(manHour)
                    )

                    // Get map of month
                    val map = viewModel.calculateMapOfMonth(tasks)
                    binding.calendarView.updateCalendar(map)
                    selectShift = map

                    // Get summary shift
                    val summaryShift = viewModel.calculateShift(tasks)
                    summaryShiftAdapter.addAll(summaryShift)
                    binding.txtSummaryShift.text = "Tổng số ca làm: " + tasks.size
                }

                Status.ERROR -> {
                    binding.pbLoading.visibility = View.GONE
                }
            }
        })
    }
}