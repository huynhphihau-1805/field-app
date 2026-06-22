package com.crayon.fieldapp.ui.screen.monitor.attendance.listTask

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.data.remote.response.TaskType
import com.crayon.fieldapp.databinding.FragmentListAttendanceAtStoreBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.dialog.filterRole.FilterRoleDialog
import com.crayon.fieldapp.ui.base.dialog.filterStore.model.ItemStore
import com.crayon.fieldapp.ui.base.dialog.selectJobByDay.SelectDatePickerDialog
import com.crayon.fieldapp.ui.screen.monitor.attendance.listTask.adapter.AttendanceAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import studio.phillip.yolo.utils.TimeFormatUtils
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors

class ListAttendanceAtStoreFragment() :
    BaseFragment<FragmentListAttendanceAtStoreBinding, ListAttendanceAtStoreViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_list_attendance_at_store

    var agencyId: String? = null
    var projectId: String? = null
    var projectName: String? = null
    var mTasks: ArrayList<TaskResponse> = arrayListOf()
    private var calendar = Calendar.getInstance()
    private var mAdapter: AttendanceAdapter? = null
    var filterStoreIds: ArrayList<String>? = null
    private var mIsLoading = false
    private var mIsEndList = false
    private var pastVisiblesItems = 0
    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0
    private var skip: Int = 0

    override val viewModel: ListAttendanceAtStoreViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
        projectId = requireArguments().get("projectId").toString()
        projectName = requireArguments().get("projectName").toString()

        mAdapter = AttendanceAdapter(
            arrayListOf(),
            requireContext(),
            itemClickListener = {
                val taskJSon = Gson().toJson(it)
                toTaskDetail(taskJSon)
            }
        )
        refresh()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.icSelectDate.setSingleClick {
            val datepicker = SelectDatePickerDialog(
                agencyId = agencyId.toString(),
                projectId = projectId.toString(),
                taskType = TaskType.TIME_KEEPING.value, itemClickListener = {
                    calendar = it
                    formatTime()
                }
            )
            datepicker.show(childFragmentManager, datepicker.getTag())
        }

        binding.tvTitle.text = projectName
        binding.txtStartDate.text = TimeFormatUtils.formatDate(calendar.time)
        binding.imbIcBack.setSingleClick {
            findNavController().popBackStack()
        }

        binding.btnFilterRole.setSingleClick {
            mTasks?.let {
                val storeDialog = FilterRoleDialog({ listRoleIds ->
                    filterStoreIds = listRoleIds
                    if (listRoleIds.isNotEmpty()) {
                        mTasks?.let { listTasks ->
                            val filter = listTasks.filter { task ->
                                listRoleIds.contains(task.job!!.role)
                            }
                            mAdapter?.clearAll()
                            mAdapter?.addAll(filter)
                            binding.txtFilterRoleStatus.visibility = View.VISIBLE
                        }
                    } else {
                        mTasks?.let {
                            mAdapter?.clearAll()
                            mAdapter?.addAll(it)
                            binding.txtFilterRoleStatus.visibility = View.GONE
                        }
                    }
                }, {
                    // Clear all
                    mAdapter?.clearAll()
                    filterStoreIds = null
                    mTasks?.let {
                        mAdapter?.addAll(it)
                        binding.txtFilterRoleStatus.visibility = View.GONE
                    }
                })
                val bundle = Bundle()
                val listRole = ArrayList<ItemStore>()
                it.forEach {
                    listRole.add(
                        ItemStore(
                            id = it.job!!.role.toString(),
                            name = it.job!!.role.toString(),
                            isSelect = filterStoreIds?.contains(it.job!!.role.toString()) ?: false
                        )
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    bundle.putSerializable(
                        "role",
                        listRole.stream().distinct().collect(Collectors.toList()) as Serializable?
                    )
                } else {
                    bundle.putSerializable("role", listRole)
                }
                storeDialog.arguments = bundle
                storeDialog.show(childFragmentManager, storeDialog.tag)
            }
        }

        binding.rvMembers.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = mAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) { //check for scroll down
                        visibleItemCount = (layoutManager as LinearLayoutManager).getChildCount()
                        totalItemCount = (layoutManager as LinearLayoutManager).getItemCount()
                        pastVisiblesItems =
                            (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (!mIsLoading && !mIsEndList) {
                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                                mIsLoading = true
                                skip = skip + 20
                                loadMoreItem(skip)
                            }
                        }
                    }
                }
            })
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.apply {
            tasks.observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let {
                    when (it.status) {
                        Status.LOADING -> {
                            mIsLoading = true
                            binding.pbLoading.visibility = View.VISIBLE
                        }
                        Status.SUCCESS -> {
                            mIsLoading = false
                            binding.pbLoading.visibility = View.GONE
                            it.data?.let { mListTasks ->
                                if (mListTasks.size != 0) {
                                    mIsEndList = false
                                    mTasks?.addAll(mListTasks)
                                    mAdapter?.addAll(mListTasks)
                                } else {
                                    mIsEndList = true
                                }
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

    private fun toTaskDetail(task: String) {
        val bundel = bundleOf("task" to task)
        findNavController().navigate(R.id.action_list_attendance_to_detail_attendance, bundel)
    }

    private fun formatTime() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.txtStartDate.text = sdf.format(calendar.time).toString()
        mAdapter?.clearAll()
        mTasks.clear()
        skip = 0
        mIsEndList = false
        viewModel.getTaskByProject(
            agencyId = agencyId.toString(),
            projectId = projectId.toString(),
            taskType = TaskType.TIME_KEEPING.value,
            date = calendar,
            skip = skip,
            take = 20
        )
    }

    private fun loadMoreItem(skip: Int) {
        viewModel.getTaskByProject(
            agencyId = agencyId.toString(),
            projectId = projectId.toString(),
            taskType = TaskType.TIME_KEEPING.value,
            date = calendar,
            skip = skip,
            take = 20
        )
    }

    private fun refresh() {
        skip = 0
        mIsEndList = false
        mAdapter?.clearAll()
        mTasks?.clear()
        viewModel.getTaskByProject(
            agencyId = agencyId.toString(),
            projectId = projectId.toString(),
            taskType = TaskType.TIME_KEEPING.value,
            date = calendar,
            skip = skip,
            take = 20
        )
    }
}