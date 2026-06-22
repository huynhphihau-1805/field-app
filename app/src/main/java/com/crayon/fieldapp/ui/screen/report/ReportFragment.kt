package com.crayon.fieldapp.ui.screen.report

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentReportBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.BaseVPAdapter
import com.crayon.fieldapp.ui.screen.report.project.ReportByProjectFragment
import com.crayon.fieldapp.ui.screen.report.time.ReportByTimeFragment
import com.crayon.fieldapp.utils.setSingleClick
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReportFragment : BaseFragment<FragmentReportBinding, ReportViewModel>() {

    override val layoutId: Int = R.layout.fragment_report

    override val viewModel: ReportViewModel by viewModel()

    private lateinit var reportAdapter: BaseVPAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabLayout = view.findViewById(R.id.tab_timekeeping)
        viewPager = view.findViewById(R.id.vp_timekeeping)

        setupViewPager()
        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }
    }

    private fun setupViewPager() {
        reportAdapter = BaseVPAdapter(childFragmentManager)
        reportAdapter.addFragment(ReportByProjectFragment(), "Dự án")
        reportAdapter.addFragment(ReportByTimeFragment(), "Thời gian")

        viewPager.apply {
            offscreenPageLimit = 2
            adapter = reportAdapter
        }
        tabLayout!!.setupWithViewPager(viewPager)
    }
}