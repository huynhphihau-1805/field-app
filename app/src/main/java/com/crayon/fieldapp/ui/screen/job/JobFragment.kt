package com.crayon.fieldapp.ui.screen.job

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.model.JobStatus
import com.crayon.fieldapp.databinding.FragmentJobBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.BaseVPAdapter
import com.crayon.fieldapp.ui.screen.job.listJob.ListJobFragment
import com.crayon.fieldapp.ui.screen.job.request.ListJobRequestFragment
import com.crayon.fieldapp.utils.setSingleClick
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class JobFragment : BaseFragment<FragmentJobBinding, JobViewModel>() {

    override val layoutId: Int = R.layout.fragment_job

    override val viewModel: JobViewModel by viewModel()
    private lateinit var mAdapter: BaseVPAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tb_jobs)
        viewPager = view.findViewById(R.id.vp_jobs)

        setupViewPager()
        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }
    }


    private fun setupViewPager() {
        mAdapter = BaseVPAdapter(childFragmentManager)
        val pending = ListJobFragment()
        val pendingStatus = Bundle()
        pendingStatus.putString("status", JobStatus.Pending.name)
        pending.arguments = pendingStatus

        val completed = ListJobFragment()
        val completeStatus = Bundle()
        completeStatus.putString("status", JobStatus.Closed.name)
        completed.arguments = completeStatus

        val request = ListJobRequestFragment()
        completed.arguments = completeStatus

        mAdapter.addFragment(pending, "Sắp chạy")
        mAdapter.addFragment(completed, "Đã đóng")

        viewPager.apply {
            offscreenPageLimit = 2
            adapter = mAdapter
        }
        tabLayout!!.setupWithViewPager(viewPager)
    }
}