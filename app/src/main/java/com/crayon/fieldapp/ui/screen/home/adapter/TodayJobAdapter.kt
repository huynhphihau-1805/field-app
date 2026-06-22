package com.crayon.fieldapp.ui.screen.home.adapter

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.JobResponse
import com.crayon.fieldapp.databinding.ItemJobBinding
import com.crayon.fieldapp.utils.formatHour
import com.crayon.fieldapp.utils.isAM
import com.crayon.fieldapp.utils.isPreviousDay
import com.crayon.fieldapp.utils.isToday
import com.crayon.fieldapp.utils.toTimeLong
import java.util.Calendar

class TodayJobAdapter constructor(
    val items: ArrayList<JobResponse>,
    val context: Context,
    val itemClickListener: (JobResponse) -> Unit = {}
) : RecyclerView.Adapter<TodayJobAdapter.JobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemJobBinding.inflate(inflater, parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val data = items[position]
        val now = Calendar.getInstance().timeInMillis
        val startTimeJob = data.startTime.toString().toTimeLong("yyyy-MM-dd'T'HH:mm") ?: 0

        if (startTimeJob.isToday()) {
            val threadhold_start = startTimeJob - 60 * 60 * 1000 // 1 hour before
            Log.d("AAAHAU", "now: $now / threadhold_start: $threadhold_start start: $startTimeJob")
            if (now >= threadhold_start) {
                enableJob(holder)
            } else {
                disableJob(holder, isClick = false)
            }
        } else {
            if (startTimeJob.isPreviousDay()) {
                disableJob(holder, isClick = false)
            } else {
                disableJob(holder, isClick = true)
            }
        }
        data.store?.name?.let {
            holder.binding.txtStoreName.text = it
        }

        data.project?.name?.let {
            holder.binding.txtProjectName.text = it
        }
        val start_date = formatHour(data.startTime.toString())
        val end_date = formatHour(data.endTime.toString())
        val timeAM = if (startTimeJob.isAM()) "Sáng" else "Chiều"

        holder.binding.root.setOnClickListener {
            itemClickListener(items[holder.adapterPosition])
        }

        val shift = "$start_date-$end_date"
        holder.binding.txtShift.text = shift
        holder.binding.txtAm.text = "($timeAM)"
        data.numOfImage?.let {
            holder.binding.txtImage.text = it.toString()
        }
    }

    inner class JobViewHolder(val binding: ItemJobBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return items.size
    }

    fun addAll(jobs: ArrayList<JobResponse>) {
        items.addAll(jobs)
        notifyDataSetChanged()
    }

    fun clearAll() {
        items.clear()
        notifyDataSetChanged()
    }

    private fun enableJob(holder: JobViewHolder) {
        holder.binding.txtStoreName.setTextColor(
            context.resources.getColor(
                R.color.colorAccent,
                null
            )
        )
        holder.binding.txtProjectName.setTextColor(
            context.resources.getColor(
                R.color.colorAccent,
                null
            )
        )
        holder.binding.txtShift.setTextColor(context.resources.getColor(R.color.colorAccent, null))
        holder.binding.txtAm.setTextColor(context.resources.getColor(R.color.colorAccent, null))
        holder.binding.txtAm.setTypeface(holder.binding.txtAm.typeface, Typeface.BOLD)
        holder.binding.root.isEnabled = true
    }

    private fun disableJob(holder: JobViewHolder, isClick: Boolean) {
        holder.binding.txtStoreName.setTextColor(
            context.resources.getColor(
                R.color.colorGray,
                null
            )
        )
        holder.binding.txtProjectName.setTextColor(
            context.resources.getColor(
                R.color.colorGray,
                null
            )
        )
        holder.binding.txtShift.setTextColor(context.resources.getColor(R.color.colorGray, null))
        holder.binding.txtAm.setTextColor(context.resources.getColor(R.color.colorGray, null))
        holder.binding.txtAm.setTypeface(holder.binding.txtAm.typeface, Typeface.NORMAL)
        holder.binding.root.isEnabled = isClick
    }
}