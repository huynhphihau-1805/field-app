package com.crayon.fieldapp.ui.screen.monitor.reportTracking

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.model.ProjectStatus
import com.crayon.fieldapp.databinding.FragmentManageReportTrackingBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.BaseVPAdapter
import com.crayon.fieldapp.ui.screen.monitor.listProject.ListProjectFragment
import com.crayon.fieldapp.utils.setSingleClick
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManageReportTrackingFragment :
    BaseFragment<FragmentManageReportTrackingBinding, ManageReportTrackingViewModel>() {

    override val layoutId: Int = R.layout.fragment_manage_report_tracking

    override val viewModel: ManageReportTrackingViewModel by viewModel()

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
        val pendingFragment = ListProjectFragment(ListProjectFragment.FROM_REPORT_TRACKING_MODE)
        val processingFragment = ListProjectFragment(ListProjectFragment.FROM_REPORT_TRACKING_MODE)
        val closedFragment = ListProjectFragment(ListProjectFragment.FROM_REPORT_TRACKING_MODE)
        val pendingBundle = bundleOf("agencyId" to agencyId, "status" to ProjectStatus.Pending)
        val processingBundle =
            bundleOf("agencyId" to agencyId, "status" to ProjectStatus.Processing)
        val closedBundle = bundleOf("agencyId" to agencyId, "status" to ProjectStatus.Closed)
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
        tabLayout.setupWithViewPager(viewPager)
    }
}