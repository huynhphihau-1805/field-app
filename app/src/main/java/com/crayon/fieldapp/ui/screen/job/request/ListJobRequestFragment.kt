package com.crayon.fieldapp.ui.screen.job.request

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.JobRequestStatus
import com.crayon.fieldapp.databinding.FragmentListJobRequestBinding
import com.crayon.fieldapp.fcm.MyFirebaseMessagingService
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.job.request.adapter.JobRequest
import com.crayon.fieldapp.ui.screen.job.request.adapter.JobsRequestRVAdapter
import com.crayon.fieldapp.utils.Status
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListJobRequestFragment :
    BaseFragment<FragmentListJobRequestBinding, ListJobRequestViewModel>() {

    override val layoutId: Int = R.layout.fragment_list_job_request
    override val viewModel: ListJobRequestViewModel by viewModel()
    private var adapterJobReqeust: JobsRequestRVAdapter? = null


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapterJobReqeust = JobsRequestRVAdapter(requireContext(), arrayListOf())

        binding.rvJobRequest.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapterJobReqeust
        }

        binding.cbSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
            adapterJobReqeust?.selectAllItems(isChecked)
        }

        binding.refresh.setOnRefreshListener {
            binding.refresh.isRefreshing = false
            refreshData()
        }



        binding.btnAccept.setOnClickListener {
            var ids = adapterJobReqeust?.getSelectedItem() as ArrayList
            viewModel.acceptMemberRequest(ids)
        }

        binding.btnReject.setOnClickListener {
            var ids = adapterJobReqeust?.getSelectedItem() as ArrayList
            viewModel.rejectMemberRequest(ids)
        }

        viewModel.apply {
            jobs.observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let {
                    when (it.status) {
                        Status.LOADING -> {
                            binding.pbLoading.visibility = View.VISIBLE
                        }
                        Status.SUCCESS -> {
                            binding.pbLoading.visibility = View.GONE
                            it.data?.let { projects ->
                                if (projects.size == 0) {
                                    binding.txtEmpty.visibility = View.VISIBLE
                                    binding.rvJobRequest.visibility = View.GONE
                                    binding.cbSelectAll.visibility = View.GONE
                                    binding.btnAccept.visibility = View.GONE
                                    binding.btnReject.visibility = View.GONE
                                } else {
                                    binding.txtEmpty.visibility = View.GONE
                                    binding.rvJobRequest.visibility = View.VISIBLE
                                    binding.cbSelectAll.visibility = View.VISIBLE
                                    binding.btnAccept.visibility = View.VISIBLE
                                    binding.btnReject.visibility = View.VISIBLE
                                }

                                var sortList = ArrayList<JobRequest>()
                                var storeIds =
                                    projects.distinctBy { it.store!!.id }.map { it.store!!.id }

                                storeIds.forEach { storeId ->
                                    var job = JobRequest("", storeId)
                                    val sameStore = ArrayList<JobRequest>()
                                    projects.forEach {
                                        if (it.store!!.id.equals(storeId)) {
                                            sameStore.add(
                                                JobRequest(
                                                    id = it.id!!,
                                                    project = it.project!!.name,
                                                    storeName = it.store!!.name,
                                                    storeId = it.store!!.id,
                                                    startShift = it.startTime,
                                                    endShift = it.endTime,
                                                    agency = it.agency!!.name
                                                )
                                            )
                                        }
                                    }
                                    sortList.add(job)
                                    sortList.addAll(sameStore)
                                }
                                adapterJobReqeust?.addAll(sortList)
                            }
                        }
                        Status.ERROR -> {
                            binding.pbLoading.visibility = View.GONE
                        }
                    }
                }
            })

            isAcceptSuccess.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        showLoading()
                    }
                    Status.SUCCESS -> {
                        hideLoading()
                        refreshData()
                    }
                    Status.ERROR -> {
                        hideLoading()
                    }
                }
            })

            isRejectSuccess.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        showLoading()
                    }
                    Status.SUCCESS -> {
                        hideLoading()
                        refreshData()
                    }
                    Status.ERROR -> {
                        hideLoading()
                    }
                }
            })
            getJobRequest("Pending")
        }
    }

    private fun refreshData() {
        binding.cbSelectAll.isChecked = false
        adapterJobReqeust?.clear()
        viewModel.getJobRequest(JobRequestStatus.Pending.value)
    }

    private val mPushFilter = IntentFilter(MyFirebaseMessagingService.PUSH_ANNOUNCE)

    override fun onResume() {
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mPushReceiver, mPushFilter)
        super.onResume()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mPushReceiver)
        super.onPause()
    }


    private val mPushReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("AAA", "onReceive")
            refreshData()
        }
    }

}