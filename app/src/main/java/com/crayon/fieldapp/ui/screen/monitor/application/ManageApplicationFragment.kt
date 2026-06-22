package com.crayon.fieldapp.ui.screen.monitor.application

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentManageApplicationBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.BaseVPAdapter
import com.crayon.fieldapp.ui.screen.monitor.application.list.ListManagementApplicationFragment
import com.crayon.fieldapp.utils.setSingleClick
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManageApplicationFragment :
    BaseFragment<FragmentManageApplicationBinding, ManageApplicationViewModel>() {

    override val layoutId: Int = R.layout.fragment_manage_application

    override val viewModel: ManageApplicationViewModel by viewModel()

    lateinit var agencyId: String
    lateinit var projectId: String

    private lateinit var mAdapter: BaseVPAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tab_application)
        viewPager = view.findViewById(R.id.vp_application)

        setupViewPager()
        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }
    }


    private fun setupViewPager() {
        mAdapter = BaseVPAdapter(childFragmentManager)
        val waiting = ListManagementApplicationFragment()
        val approved = ListManagementApplicationFragment()
        waiting.arguments = bundleOf("status" to "wait", "agencyId" to agencyId)
        approved.arguments = bundleOf("status" to "approved", "agencyId" to agencyId)
        mAdapter.addFragment(waiting, "Chờ duyệt")
        mAdapter.addFragment(approved, "Đã duyệt")

        viewPager.apply {
            offscreenPageLimit = 2
            adapter = mAdapter
        }
        tabLayout.setupWithViewPager(viewPager)
    }
}