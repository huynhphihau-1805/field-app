package com.crayon.fieldapp.ui.screen.report.time

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.databinding.FragmentReportTimeBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.report.adapter.SimpleRVAdapter
import com.crayon.fieldapp.ui.screen.report.adapter.TodayShiftRVAdapter
import com.crayon.fieldapp.ui.widgets.MtsCalendarView
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.toTimeString
import org.koin.androidx.viewmodel.ext.android.viewModel
import studio.phillip.yolo.utils.TimeFormatUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ReportByTimeFragment :
    BaseFragment<FragmentReportTimeBinding, ReportTimeViewModel>() {
    private lateinit var selectShift: HashMap<Date, ArrayList<TaskResponse>>

    override val layoutId: Int = R.layout.fragment_report_time

    override val viewModel: ReportTimeViewModel by viewModel()

    var calendar = Calendar.getInstance()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var projectAdapter = SimpleRVAdapter(arrayListOf(), requireContext())
        var summaryShiftAdapter = SimpleRVAdapter(arrayListOf(), requireContext())
        var todayShiftadapter = TodayShiftRVAdapter(arrayListOf(), true, requireContext())

        binding.rvTodayShift.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.rvTodayShift.adapter = todayShiftadapter

        binding.rvProject.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.rvProject.adapter = projectAdapter

        binding.rvSummaryShift.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.rvSummaryShift.adapter = summaryShiftAdapter


        binding.calendarView.setEventHandler(object : MtsCalendarView.EventHandler {
            override fun onMonthPress(cal: Calendar) {
                calendar = cal
                viewModel.getReportByTime(cal)
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

        viewModel.apply {
            getReportByTime(calendar)
            listTask.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let { tasks ->
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

                            // Get project
                            val projects = viewModel.getProjectOfReport(tasks)
                            binding.txtSummaryProject.text = "Số dự án: " + projects.size
                            projectAdapter.addAll(projects)

                            // Get total man Hour
                            val manHour = viewModel.calculateTotalManHour(tasks)
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
                    }
                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            })
        }
    }
}