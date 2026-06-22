package com.crayon.fieldapp.ui.screen.monitor.changeGift.listTask

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
import com.crayon.fieldapp.databinding.FragmentListChangeGiftAtStoreBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.dialog.filterStore.FilterStoreDialog
import com.crayon.fieldapp.ui.base.dialog.filterStore.model.ItemStore
import com.crayon.fieldapp.ui.base.dialog.selectJobByDay.SelectDatePickerDialog
import com.crayon.fieldapp.ui.screen.monitor.changeGift.listTask.adapter.ManageChangeGiftRVAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.toTimeString
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import studio.phillip.yolo.utils.TimeFormatUtils
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.stream.Collectors

class ListChangeGiftAtStoreFragment() :
    BaseFragment<FragmentListChangeGiftAtStoreBinding, ListChangeGiftAtStoreViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_list_change_gift_at_store

    var agencyId: String? = null
    var projectId: String? = null
    var projectName: String? = null
    private var calendar = Calendar.getInstance()
    var filterStoreIds: ArrayList<String> = arrayListOf()
    var mTasks: ArrayList<TaskResponse> = arrayListOf()
    private var mAdapter: ManageChangeGiftRVAdapter? = null
    private var mIsLoading = false
    private var mIsEndList = false
    private var pastVisiblesItems = 0
    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0
    private var skip: Int = 0

    override val viewModel: ListChangeGiftAtStoreViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
        projectId = requireArguments().get("projectId").toString()
        projectName = requireArguments().get("projectName").toString()

        mAdapter = ManageChangeGiftRVAdapter(
            arrayListOf(),
            requireContext(),
            itemClickListener = {
                val taskJSon = Gson().toJson(it)
                toTaskDetail(taskJSon)
            }
        )

        viewModel.getTaskByProject(
            agencyId = agencyId.toString(),
            projectId = projectId.toString(),
            taskType = TaskType.CHANGE_GIFT.value,
            date = calendar,
            skip = 0,
            take = 20
        )
        viewModel.getProjectSummary(
            agencyId = agencyId.toString(),
            projectId = projectId.toString(),
            date = calendar
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.icSelectDate.setSingleClick {
            val datepicker = SelectDatePickerDialog(
                agencyId = agencyId.toString(),
                projectId = projectId.toString(),
                taskType = TaskType.CHANGE_GIFT.value,
                itemClickListener = {
                    calendar = it
                    formatTime()
                }
            )
            datepicker.show(childFragmentManager, datepicker.getTag())
        }

        binding.btnCustomer.setSingleClick {
            val start_date = TimeFormatUtils.getDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                0,
                0
            )!!.toTimeString("yyyy-MM-dd") + "T00:00:00.000Z"
            val end_date = TimeFormatUtils.getDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                23,
                59
            )!!.toTimeString("yyyy-MM-dd") + "T23:59:00.000Z"
            findNavController().navigate(
                R.id.action_list_changeGift_to_listCustomer,
                bundleOf(
                    "agencyId" to agencyId.toString(), "projectId" to projectId.toString(),
                    "startDate" to start_date, "endDate" to end_date
                )
            )
        }

        binding.btnPromotion.setSingleClick {
            val start_date = TimeFormatUtils.getDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                0,
                0
            )!!.toTimeString("yyyy-MM-dd") + "T00:00:00.000Z"
            val end_date = TimeFormatUtils.getDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                23,
                59
            )!!.toTimeString("yyyy-MM-dd") + "T23:59:00.000Z"
            findNavController().navigate(
                R.id.action_list_changeGift_to_listPromotion,
                bundleOf(
                    "agencyId" to agencyId.toString(), "projectId" to projectId.toString(),
                    "startDate" to start_date, "endDate" to end_date
                )
            )
        }

        binding.btnGift.setSingleClick {
            val start_date = TimeFormatUtils.getDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                0,
                0
            )!!.toTimeString("yyyy-MM-dd") + "T00:00:00.000Z"
            val end_date = TimeFormatUtils.getDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                23,
                59
            )!!.toTimeString("yyyy-MM-dd") + "T23:59:00.000Z"
            findNavController().navigate(
                R.id.action_list_changeGift_to_listGift,
                bundleOf(
                    "agencyId" to agencyId.toString(),
                    "projectId" to projectId.toString(),
                    "startDate" to start_date,
                    "endDate" to end_date
                )
            )
        }

        binding.btnFilterStore.setSingleClick {
            mTasks?.let {
                val storeDialog = FilterStoreDialog({ listRoleIds ->
                    filterStoreIds.clear()
                    filterStoreIds.addAll(listRoleIds)
                    if (listRoleIds.isNotEmpty()) {
                        mTasks?.let { listTasks ->
                            val filter = listTasks.filter { task ->
                                listRoleIds.contains(task.store!!.id.toString())
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
                    filterStoreIds.clear()
                    mAdapter?.clearAll()
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
                            id = it.store!!.id.toString(),
                            name = it.store!!.name.toString(),
                            isSelect = filterStoreIds.contains(it.store!!.id.toString()) ?: false
                        )
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    bundle.putSerializable(
                        "store",
                        listRole.stream().distinct().collect(Collectors.toList()) as Serializable?
                    )
                } else {
                    bundle.putSerializable("store", listRole)
                }
                storeDialog.arguments = bundle
                storeDialog.show(childFragmentManager, storeDialog.tag)
            }
        }

        binding.tvTitle.text = projectName
        binding.txtStartDate.text = TimeFormatUtils.formatDate(calendar.time)
        binding.imbIcBack.setSingleClick {
            findNavController().popBackStack()
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
            listTask.observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let {
                    when (it.status) {
                        Status.LOADING -> {
                            mIsLoading = true
                            binding.pbLoading.visibility = View.VISIBLE
                        }

                        Status.SUCCESS -> {
                            mIsLoading = false

                            binding.pbLoading.visibility = View.GONE
                            binding.rvMembers.visibility = View.VISIBLE
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
            summary.observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let {
                    when (it.status) {
                        Status.LOADING -> {
                            binding.pbLoading.visibility = View.VISIBLE
                        }

                        Status.SUCCESS -> {
                            binding.pbLoading.visibility = View.GONE
                            it.data?.let {
                                binding.txtNumCustomer.text = it.totalCustomer.toString() + " người"
                                binding.txtNumPromotion.text = it.totalPromotion.toString() + " gói"
                                binding.txtGiftCustomer.text = it.totalGift.toString() + " phần"
                            }
                        }

                        Status.ERROR -> {
                            binding.pbLoading.visibility = View.GONE
                        }
                    }
                }
            })
        }
    }

    private fun toTaskDetail(task: String) {
        val bundel = bundleOf("task" to task)
        findNavController().navigate(
            R.id.action_list_changeGift_to_detail_changeGift,
            bundel
        )
    }

    private fun formatTime() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.txtStartDate.text = sdf.format(calendar.time).toString()
        mTasks.clear()
        mAdapter?.clearAll()
        skip = 0
        mIsEndList = false
        viewModel.getTaskByProject(
            agencyId = agencyId.toString(),
            projectId = projectId.toString(),
            taskType = TaskType.CHANGE_GIFT.value,
            date = calendar,
            skip = skip,
            take = 20
        )
        viewModel.getProjectSummary(
            agencyId = agencyId.toString(),
            projectId = projectId.toString(),
            date = calendar
        )
    }


    private fun loadMoreItem(skip: Int) {
        viewModel.getTaskByProject(
            agencyId = agencyId.toString(),
            projectId = projectId.toString(),
            taskType = TaskType.CHANGE_GIFT.value,
            date = calendar,
            skip = skip,
            take = 20
        )
    }
}