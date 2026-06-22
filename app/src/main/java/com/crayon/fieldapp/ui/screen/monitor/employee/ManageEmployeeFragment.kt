package com.crayon.fieldapp.ui.screen.monitor.employee

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentManageEmployeeBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.BaseVPAdapter
import com.crayon.fieldapp.ui.screen.monitor.employee.listGroupMember.ListGroupEmployeeFragment
import com.crayon.fieldapp.ui.screen.monitor.employee.listMembers.ListEmployeeFragment
import com.crayon.fieldapp.utils.setSingleClick
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManageEmployeeFragment :
    BaseFragment<FragmentManageEmployeeBinding, ManageEmployeeViewModel>() {

    override val layoutId: Int = R.layout.fragment_manage_employee

    override val viewModel: ManageEmployeeViewModel by viewModel()

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

        tabLayout = view.findViewById(R.id.tab_employee)
        viewPager = view.findViewById(R.id.vp_employee)

        setupViewPager()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }
    }

    private fun setupViewPager() {
        mAdapter = BaseVPAdapter(childFragmentManager)
        val listGroupFragment = ListGroupEmployeeFragment()
        val listStoreFragment = ListEmployeeFragment()
        val bundle = Bundle()
        bundle.putString("agencyId", agencyId)
        listGroupFragment.arguments = bundle
        listStoreFragment.arguments = bundle
        mAdapter.addFragment(listGroupFragment, "Phòng ban")
        mAdapter.addFragment(listStoreFragment, "Nhân viên")

        viewPager.apply {
            offscreenPageLimit = 2
            adapter = mAdapter
        }
        tabLayout!!.setupWithViewPager(viewPager)
    }
}