package com.crayon.fieldapp.ui.screen.monitor.attendance.listTask.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.databinding.ItemJobMonitorAttendanceBinding
import com.crayon.fieldapp.utils.formatHour
import com.crayon.fieldapp.utils.toTimeLong
import com.crayon.fieldapp.utils.toTimeString
import java.util.*

class AttendanceAdapter constructor(
    val items: ArrayList<TaskResponse>,
    val context: Context,
    val itemClickListener: (TaskResponse) -> Unit = {}
) : RecyclerView.Adapter<AttendanceAdapter.JobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemJobMonitorAttendanceBinding.inflate(inflater, parent, false)
        val holder = JobViewHolder(binding)
        holder.itemView.setOnClickListener {
            itemClickListener(items[holder.absoluteAdapterPosition])
        }
        return holder
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val data = items[position]
        holder.bind(data)
    }

    inner class JobViewHolder(private val binding: ItemJobMonitorAttendanceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: TaskResponse) {
            binding.txtStoreName.text = ""
            binding.txtShift.text = ""
            binding.txtCheckOutValue.text = ""
            binding.txtCheckInValue.text = ""
            binding.txtPic.text = ""

            var checkIn = 0L
            var checkOut = 0L
            val currentTime = Calendar.getInstance().timeInMillis
            val startTime = data.job!!.startTime.toString().toTimeLong("yyyy-MM-dd'T'HH:mm") ?: 0
            val endTime = data.job!!.endTime.toString().toTimeLong("yyyy-MM-dd'T'HH:mm") ?: 0

            data.attendances?.let { attendances ->
                if (attendances.size == 1) {
                    binding.txtCheckInValue.text = formatHour(attendances[0].checkInTime.toString())
                    binding.txtCheckOutValue.text = formatHour(attendances[0].checkOutTime.toString())
                    checkIn = attendances[0].checkInTime.toString().toTimeLong("yyyy-MM-dd'T'HH:mm") ?: 0
                    checkOut = attendances[0].checkOutTime.toString().toTimeLong("yyyy-MM-dd'T'HH:mm") ?: 0
                }
            }

            if (checkIn > startTime) {
                binding.txtCheckInValue.setTextColor(context.resources.getColor(R.color.colorRed, null))
            } else {
                if (checkIn == 0L) {
                    binding.txtCheckInValue.setTextColor(context.resources.getColor(R.color.colorRed, null))
                    binding.txtCheckInValue.text = "InComplete"
                } else {
                    binding.txtCheckInValue.setTextColor(context.resources.getColor(R.color.colorGray, null))
                }
            }

            if (checkOut < endTime) {
                binding.txtCheckOutValue.setTextColor(context.resources.getColor(R.color.colorRed, null))
                if (checkOut == 0L) {
                    binding.txtCheckOutValue.text = "InComplete"
                }
            } else {
                binding.txtCheckOutValue.setTextColor(context.resources.getColor(R.color.colorGray, null))
            }

            data.store?.name?.let {
                binding.txtStoreName.text = it
            }

            data.pic?.let {
                binding.txtPic.text = it.lastName + " " + it.firstName
            }

            val start_date = formatHour(data.job!!.startTime.toString())
            val end_date = formatHour(data.job!!.endTime.toString())
            var timeAM = "Sáng"
            if (startTime.toTimeString("yyyy-MM-dd aa")!!.contains("AM")) {
                timeAM = "Sáng"
            } else {
                timeAM = "Chiều"
            }

            val shift = "$start_date-$end_date"
            binding.txtShift.text = shift
            binding.txtAm.text = "($timeAM)"
        }
    }

    override fun getItemCount(): Int {
        return this.items.size
    }

    fun addAll(jobs: List<TaskResponse>) {
        items.addAll(jobs)
        notifyDataSetChanged()
    }

    fun clearAll(){
        items.clear()
        notifyDataSetChanged()
    }
}
