package com.crayon.fieldapp.ui.screen.home

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.JobResponse
import com.crayon.fieldapp.data.remote.response.RoleOfAgency
import com.crayon.fieldapp.databinding.FragmentHomeBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.dialog.filterStore.FilterStoreDialog
import com.crayon.fieldapp.ui.base.dialog.filterStore.model.ItemStore
import com.crayon.fieldapp.ui.screen.home.adapter.TodayJobAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.getCurrentDateTime
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.toTimeString
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.Serializable
import java.util.stream.Collectors

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    override val layoutId: Int = R.layout.fragment_home
    override val viewModel: HomeViewModel by viewModel()

    var jobAdapter: TodayJobAdapter? = null
    var jobs: ArrayList<JobResponse> = arrayListOf()
    var filterStoreIds: ArrayList<String>? = null
    private var isLoading = false
    private var isEndList = false
    private var pastVisiblesItems = 0
    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0
    private var skip: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("HAUHAUA", "onCreate")
        super.onCreate(savedInstanceState)
        jobAdapter = TodayJobAdapter(
            arrayListOf(),
            requireContext(),
            itemClickListener = { toJobDetail(it.id.toString()) }
        )
        viewModel.getListRole()
        viewModel.deleteProduct()
    }

    override fun onResume() {
        super.onResume()
        refresh()
        jobAdapter?.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("HAUHAUA", "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
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
                        if (!isLoading && !isEndList) {
                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                                isLoading = true
                                skip = skip + 200
                                loadMoreItem(skip)
                            }
                        }
                    }
                }
            })
        }

        binding.btnReport.setSingleClick {
            findNavController().navigate(R.id.to_report)
        }

        binding.btnCalendar.setSingleClick {
            findNavController().navigate(R.id.to_calendar)
        }

        binding.btnJob.setSingleClick {
            findNavController().navigate(R.id.main_to_list_job)
        }

        binding.btnMonitoring.setSingleClick {
            findNavController().navigate(R.id.action_monitor)
        }

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            refresh()
        }
        binding.btnFilterStore.setOnClickListener {
            jobs.let {
                val storeDialog = FilterStoreDialog({ listStoreIds ->
                    filterStoreIds = listStoreIds
                    if (listStoreIds.isNotEmpty()) {
                        jobs.let { listJobs ->
                            val filter = listJobs.filter { job ->
                                listStoreIds.contains(job.store!!.id)
                            }
                            jobAdapter?.clearAll()
                            jobAdapter?.addAll(filter as ArrayList<JobResponse>)
                            binding.txtFilterStoreStatus.visibility = View.VISIBLE
                        }
                    } else {
                        jobs.let {
                            jobAdapter?.addAll(it)
                            binding.txtFilterStoreStatus.visibility = View.GONE
                        }
                    }
                }, {
                    // Clear all
                    filterStoreIds = null
                    jobAdapter?.clearAll()
                    jobs.let {
                        jobAdapter?.addAll(it)
                        binding.txtFilterStoreStatus.visibility = View.GONE
                    }
                })
                val bundle = Bundle()
                val listStore = ArrayList<ItemStore>()
                it.forEach {
                    listStore.add(
                        ItemStore(
                            id = it.store!!.id.toString(),
                            name = it.store!!.name.toString(),
                            isSelect = filterStoreIds?.contains(it.store!!.id.toString()) ?: false
                        )
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    bundle.putSerializable(
                        "store",
                        listStore.stream().distinct().collect(Collectors.toList()) as Serializable?
                    )
                } else {
                    bundle.putSerializable("store", listStore)
                }
                storeDialog.arguments = bundle
                storeDialog.show(childFragmentManager, storeDialog.tag)
            }
        }

        viewModel.jobs.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                Log.d("HAUHAUA", "getContentIfNotHandled")
                when (it.status) {
                    Status.LOADING -> {
                        Log.d("HAUHAUA", "LOADING")
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        Log.d("HAUHAUA", "SUCCESS")
                        isLoading = false
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            if (it.size == 0) {
                                isEndList = true
                            } else {
                                isEndList = false
                                jobs.addAll(it)
                                jobAdapter?.addAll(it as ArrayList<JobResponse>)
                            }
                        }
                    }

                    Status.ERROR -> {
                        isLoading = false
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.isShowMonitorTab.observe(viewLifecycleOwner, { isShow ->
            if (isShow) {
                binding.btnMonitoring.visibility = View.VISIBLE
            } else {
                binding.btnMonitoring.visibility = View.GONE
            }
        })

        viewModel.listAgency.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        val roles = it.data
                        var isShow = false
                        for (i in 0..roles!!.size - 1) {
                            if (!roles.get(i).role!!.equals(RoleOfAgency.EMPLOYEE.value)) {
                                isShow = true
                                break
                            }
                        }
                        viewModel.setIsShowMonitorTab(isShow)
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })
    }


    private fun toJobDetail(id: String) {
        Log.d("HAUAAA", id)
        val bundel = bundleOf("jobId" to id)
        findNavController().navigate(R.id.action_detail_job, bundel)
    }

    private fun refresh() {
        // 2020-08-27T00:00:00.000Z
        val startDate = getCurrentDateTime().toTimeString("yyyy-MM-dd") + "T00:00:00.000Z"
        val endDate = getCurrentDateTime().toTimeString("yyyy-MM-dd") + "T23:59:00.000Z"
        jobAdapter?.clearAll()
        jobs.clear()
        isEndList = false
        skip = 0
        viewModel.getTodayJob(startDate, endDate, skip = skip)
    }

    private fun loadMoreItem(skip: Int) {
        // 2020-08-27T00:00:00.000Z
        val startDate = getCurrentDateTime().toTimeString("yyyy-MM-dd") + "T00:00:00.000Z"
        val endDate = getCurrentDateTime().toTimeString("yyyy-MM-dd") + "T23:59:00.000Z"
        viewModel.getTodayJob(startDate, endDate, skip = skip)
    }
}