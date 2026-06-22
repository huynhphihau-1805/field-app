package com.crayon.fieldapp.ui.screen.application

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentApplicationBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.BaseVPAdapter
import com.crayon.fieldapp.ui.screen.application.list.ListPicApplicationFragment
import com.crayon.fieldapp.utils.setSingleClick
import org.koin.androidx.viewmodel.ext.android.viewModel

class ApplicationFragment :
    BaseFragment<FragmentApplicationBinding, ApplicationViewModel>() {

    override val layoutId: Int = R.layout.fragment_application
    override val viewModel: ApplicationViewModel by viewModel()

    private lateinit var mAdapter: BaseVPAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.imbIcBack.setSingleClick {
            navigateUp()
        }

        binding.fabCreate.setSingleClick {
            findNavController().navigate(R.id.action_applicationFragment_to_createApplicationFragment)
        }
    }

    private fun setupViewPager() {
        mAdapter = BaseVPAdapter(childFragmentManager)
        val waitingFragment = ListPicApplicationFragment().apply {
            arguments = bundleOf("status" to "wait")
        }
        val approvedFragment = ListPicApplicationFragment().apply {
            arguments = bundleOf("status" to "approved")
        }

        mAdapter.apply {
            addFragment(waitingFragment, "Chờ duyệt")
            addFragment(approvedFragment, "Đã duyệt")
        }

        binding.vpMyApplication.apply {
            offscreenPageLimit = 2
            adapter = mAdapter
        }
        binding.tabMyApplication.setupWithViewPager(binding.vpMyApplication)
    }
}
