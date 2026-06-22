package com.crayon.fieldapp.ui.screen.calendar

import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.JobResponse
import com.crayon.fieldapp.databinding.FragmentCalendarBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.home.adapter.TodayJobAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.getCurrentDateTime
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.toTimeString
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class CalendarFragment : BaseFragment<FragmentCalendarBinding, CalendarViewModel>(),
    CalendarView.OnDateChangeListener {

    override val layoutId: Int = R.layout.fragment_calendar

    override val viewModel: CalendarViewModel by viewModel()

    private var startDate: String? = null
    private var jobAdapter: TodayJobAdapter? = null
    private var endDate: String? = null
    private var mIsLoading = false
    private var pastVisiblesItems = 0
    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0
    private var skip: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 2020-08-27T00:00:00.000Z
        val startDate = getCurrentDateTime().toTimeString("yyyy-MM-dd") + "T00:00:00.000Z"
        val endDate = getCurrentDateTime().toTimeString("yyyy-MM-dd") + "T23:59:00.000Z"
        viewModel.getTodayJob(startDate, endDate, skip)
        jobAdapter = TodayJobAdapter(
            arrayListOf(),
            requireContext(),
            itemClickListener = { toJobDetail(it.id.toString()) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.calendar.setOnDateChangeListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.txtDate.text = "Ngày " + Calendar.getInstance().timeInMillis.toTimeString("dd/MM/yyyy")

        binding.imbIcBack?.setSingleClick {
            findNavController().navigateUp()
        }

        binding.rvJobs.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = jobAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) { //check for scroll down
                        visibleItemCount = (layoutManager as LinearLayoutManager).getChildCount()
                        totalItemCount = (layoutManager as LinearLayoutManager).getItemCount()
                        pastVisiblesItems =
                            (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (!mIsLoading) {
                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                                mIsLoading = true
                                skip = skip + 200
                                loadMoreItem(skip)
                            }
                        }
                    }
                }
            })
        }

        viewModel.apply {
            jobs.observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let {
                    when (it.status) {
                        Status.LOADING -> {
                            mIsLoading = true
                            binding.pbLoading.visibility = View.VISIBLE
                        }
                        Status.SUCCESS -> {
                            mIsLoading = false
                            binding.pbLoading.visibility = View.GONE

                            it.data?.let {
                                jobAdapter?.addAll(it as ArrayList<JobResponse>)
                            }
                        }
                        Status.ERROR -> {
                            mIsLoading = false
                            binding.pbLoading.visibility = View.GONE
                        }
                    }
                }

            })
        }
    }

    private fun toJobDetail(id: String) {
        val bundel = bundleOf("jobId" to id)
        findNavController().navigate(R.id.action_calendar_to_jobDetail, bundel)
    }

    override fun onSelectedDayChange(p0: CalendarView, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.MINUTE, 0)

        binding.txtDate.text = "Ngày " + calendar.timeInMillis.toTimeString("dd/MM/yyyy")
        startDate = calendar.timeInMillis.toTimeString("yyyy-MM-dd") + "T00:00:00.000Z"
        endDate = calendar.timeInMillis.toTimeString("yyyy-MM-dd") + "T23:59:00.000Z"
        skip = 0
        jobAdapter?.clearAll()
        viewModel.getTodayJob(startDate!!, endDate!!, skip)
    }


    private fun loadMoreItem(skip: Int) {
        if (startDate != null && endDate != null) {
            viewModel.getTodayJob(startDate!!, endDate!!, skip)
        }
    }
}