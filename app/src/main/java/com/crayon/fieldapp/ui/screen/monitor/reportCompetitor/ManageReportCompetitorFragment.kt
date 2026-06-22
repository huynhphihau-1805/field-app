package com.crayon.fieldapp.ui.screen.monitor.reportCompetitor

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.model.ProjectStatus
import com.crayon.fieldapp.data.remote.response.TaskType
import com.crayon.fieldapp.databinding.FragmentManageReportCompetitorBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.BaseVPAdapter
import com.crayon.fieldapp.ui.screen.monitor.listProject.ListProjectFragment
import com.crayon.fieldapp.utils.setSingleClick
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManageReportCompetitorFragment :
    BaseFragment<FragmentManageReportCompetitorBinding, ManageReportCompetitorViewModel>() {

    override val layoutId: Int = R.layout.fragment_manage_report_competitor

    override val viewModel: ManageReportCompetitorViewModel by viewModel()

    lateinit var agencyId: String

    private lateinit var mAdapter: BaseVPAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tab_project)
        viewPager = view.findViewById(R.id.vp_project)

        setupViewPager()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }
    }

    private fun setupViewPager() {
        val pendingFragment = ListProjectFragment(ListProjectFragment.FROM_REPORT_COMPETITOR_MODE)
        val processingFragment =
            ListProjectFragment(ListProjectFragment.FROM_REPORT_COMPETITOR_MODE)
        val closedFragment = ListProjectFragment(ListProjectFragment.FROM_REPORT_COMPETITOR_MODE)
        val pendingBundle = bundleOf(
            "agencyId" to agencyId,
            "status" to ProjectStatus.Pending,
            "type" to TaskType.REPORT_COMPITETOR.value
        )
        val processingBundle =
            bundleOf(
                "agencyId" to agencyId,
                "status" to ProjectStatus.Processing,
                "type" to TaskType.REPORT_COMPITETOR.value
            )
        val closedBundle = bundleOf(
            "agencyId" to agencyId,
            "status" to ProjectStatus.Closed,
            "type" to TaskType.REPORT_COMPITETOR.value
        )
        pendingFragment.arguments = pendingBundle
        processingFragment.arguments = processingBundle
        closedFragment.arguments = closedBundle

        mAdapter = BaseVPAdapter(childFragmentManager)
        mAdapter.addFragment(processingFragment, "Đang chạy")
        mAdapter.addFragment(pendingFragment, "Sắp chạy")
        mAdapter.addFragment(closedFragment, "Đã đóng")

        viewPager.apply {
            offscreenPageLimit = 3
            adapter = mAdapter
        }
        tabLayout!!.setupWithViewPager(viewPager)
    }
}