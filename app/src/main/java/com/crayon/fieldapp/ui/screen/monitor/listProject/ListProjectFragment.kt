package com.crayon.fieldapp.ui.screen.monitor.listProject

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.ProjectResponse
import com.crayon.fieldapp.databinding.FragmentListProjectBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.ProjectAdapter
import com.crayon.fieldapp.utils.MStringUtils
import com.crayon.fieldapp.utils.Status
import com.example.moviedb.utils.getQueryTextChangeStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListProjectFragment(private var mode: Int? = FROM_PROJECT_MODE) :
    BaseFragment<FragmentListProjectBinding, ListProjectViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_list_project

    lateinit var agencyId: String
    private var status: String? = null
    private var taskType: Int = 0

    override val viewModel: ListProjectViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
        status = requireArguments().get("status").toString()
        taskType = requireArguments().getInt("type")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.svProject.queryHint = "Tìm kiếm theo tên dự án"
        val adapter = ProjectAdapter(
            itemClickListener = { toProjectDetail(it) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.apply {
            projects.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let { list ->
                            val sort = list.sortedByDescending { it.createdAt }
                            adapter.submitList(sort)
                        }
                    }
                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            })
            getProjects(agencyId, status, taskType)
        }
        setUpSearchStateFlow()
    }

    private fun toProjectDetail(project: ProjectResponse) {
        if (mode == FROM_PROJECT_MODE) {
            val bundel = bundleOf(
                "projectId" to project.id,
                "projectName" to project.name,
                "agencyId" to agencyId
            )
            findNavController().navigate(R.id.to_detail_project, bundel)
        } else if (mode == FROM_UPDATE_STATUS_MODE) {
            val bundel = bundleOf(
                "projectId" to project.id,
                "projectName" to project.name,
                "agencyId" to agencyId
            )
            findNavController().navigate(R.id.action_updateStatus_to_list_updateStatus, bundel)
        } else if (mode == FROM_REPORT_COMPETITOR_MODE) {
            val bundel = bundleOf(
                "projectId" to project.id,
                "projectName" to project.name,
                "agencyId" to agencyId
            )
            findNavController().navigate(
                R.id.action_reportCompetitor_to_list_reportCompetitor,
                bundel
            )
        } else if (mode == FROM_REPORT_SALES_MODE) {
            val bundel = bundleOf(
                "projectId" to project.id,
                "projectName" to project.name,
                "agencyId" to agencyId
            )
            findNavController().navigate(R.id.action_reportSales_to_list_reportSales, bundel)
        } else if (mode == FROM_REPORT_TRACKING_MODE) {
            val bundel = bundleOf(
                "projectId" to project.id,
                "projectName" to project.name,
                "agencyId" to agencyId
            )
            findNavController().navigate(R.id.action_reportTracking_to_list_reportTracking, bundel)
        } else if (mode == FROM_REPORT_CHANGE_GIFT_MODE) {
            val bundel = bundleOf(
                "projectId" to project.id,
                "projectName" to project.name,
                "agencyId" to agencyId
            )
            findNavController().navigate(R.id.action_changeGift_to_list_changeGift, bundel)
        } else {
            val bundel = bundleOf(
                "projectId" to project.id,
                "projectName" to project.name,
                "agencyId" to agencyId
            )
            findNavController().navigate(R.id.action_attendance_to_list_attendance, bundel)
        }

    }

    private fun setUpSearchStateFlow() {
        GlobalScope.launch {
            binding.svProject.getQueryTextChangeStateFlow()
                .debounce(1000)
                .filter { query ->
                    if (query.isEmpty()) {
                        return@filter false
                    } else {
                        return@filter true
                    }
                }
                .distinctUntilChanged()
                .collect { result ->
                    withContext(Dispatchers.Main) {
                        binding.svProject.clearFocus()
                        if (result.isNullOrBlank()) {
                            viewModel.getProjects(agencyId, status, type = taskType)
                        } else {
                            val key_search = MStringUtils.removeAccents(result)
                            viewModel.searchProjects(
                                agencyId = agencyId,
                                name = key_search.toString(),
                                status = status
                            )
                        }
                    }
                }
        }
    }


    companion object {
        const val FROM_ATTENDANCE_MODE = 1
        const val FROM_UPDATE_STATUS_MODE = 2
        const val FROM_REPORT_COMPETITOR_MODE = 3
        const val FROM_REPORT_SALES_MODE = 4
        const val FROM_REPORT_TRACKING_MODE = 5
        const val FROM_REPORT_CHANGE_GIFT_MODE = 6
        const val FROM_PROJECT_MODE = 0
    }

}