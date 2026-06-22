package com.crayon.fieldapp.ui.base.dialog

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.crayon.fieldapp.R
import java.util.Calendar


class CustomDatePickerDialog(val events: ArrayList<CalendarDay>) : DialogFragment() {
    lateinit var picker: CalendarView
    lateinit var btnOk: Button
    lateinit var btnCancel: Button

    private var mListener: DatePickerDialogListener? = null
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.customer_select_date_picker, container, false)
            .apply {
                picker = findViewById(R.id.calendarView)

                val calendar = Calendar.getInstance()
                val event = CalendarDay(calendar)
                event.imageDrawable = resources.getDrawable(R.drawable.bg_blue_sv_rounded, null)
                events.add(event)
                picker.setCalendarDays(events)

                picker.setOnCalendarDayClickListener(object : OnCalendarDayClickListener {
                    override fun onClick(calendarDay: CalendarDay) {
                        val clickedDayCalendar: Calendar = calendarDay.calendar
                        mListener?.getDate(clickedDayCalendar)
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

    fun setListener(listener: DatePickerDialogListener) {
        this.mListener = listener
    }


    interface DatePickerDialogListener {
        fun getDate(calendar: Calendar)
    }
}
