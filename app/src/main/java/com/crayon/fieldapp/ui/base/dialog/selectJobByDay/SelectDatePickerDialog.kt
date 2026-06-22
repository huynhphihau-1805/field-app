package com.crayon.fieldapp.ui.base.dialog.selectJobByDay

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.databinding.CustomerSelectDatePickerBinding
import com.crayon.fieldapp.ui.base.BaseDialogFragment
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.toCalendar
import com.crayon.fieldapp.utils.toDate
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar


class SelectDatePickerDialog(
    val projectId: String,
    val agencyId: String,
    val taskType: Int,
    val itemClickListener: (Calendar) -> Unit = {}
) :
    BaseDialogFragment<CustomerSelectDatePickerBinding, SelectDatePickerViewModel>() {
    override val layoutId: Int = R.layout.customer_select_date_picker
    override val viewModel: SelectDatePickerViewModel by viewModel()
    var events: ArrayList<EventDay> = ArrayList()
    var calendar = Calendar.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getTaskByProject(
            calendar,
            projectId = projectId,
            agencyId = agencyId,
            taskType = taskType
        )

        viewModel.listTask.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let { mList ->
                            setJobEvents(mList as ArrayList<TaskResponse>)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })

        binding.btnOk.setSingleClick {
            itemClickListener.invoke(calendar)
            dismiss()
        }

        binding.btnCancel.setSingleClick {
            dismiss()
        }


        binding.calendarView.setOnCalendarDayClickListener(object : OnCalendarDayClickListener {
            override fun onClick(calendarDay: CalendarDay) {
                calendar = calendarDay.calendar
            }

        })

        binding.calendarView.setOnPreviousPageChangeListener(object : OnCalendarPageChangeListener {
            override fun onChange() {
                calendar.add(Calendar.MONTH, -1)
                viewModel.getTaskByProject(
                    calendar,
                    projectId = projectId,
                    agencyId = agencyId,
                    taskType = taskType
                )
            }

        })


        binding.calendarView.setOnForwardPageChangeListener(object : OnCalendarPageChangeListener {
            override fun onChange() {
                calendar.add(Calendar.MONTH, 1)
                viewModel.getTaskByProject(
                    calendar,
                    projectId = projectId,
                    agencyId = agencyId,
                    taskType = taskType
                )
            }

        })

    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {

            // Set gravity of dialog
            dialog.setCanceledOnTouchOutside(true)
            val window = dialog.window
            val wlp = window!!.attributes
            wlp.gravity = Gravity.CENTER
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.attributes = wlp
            window.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun setJobEvents(jobs: ArrayList<TaskResponse>) {
        events.clear()
        jobs.forEach {
            val date =
                it.job?.startTime?.toDate("yyyy-MM-dd'T'HH:mm")?.toCalendar()
            date?.let {
                events.add(EventDay(it, R.drawable.bg_has_job))
            }
        }
        binding.calendarView.setEvents(events)
    }


}
