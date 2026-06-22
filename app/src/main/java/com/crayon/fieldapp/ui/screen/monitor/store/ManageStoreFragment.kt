package com.crayon.fieldapp.ui.screen.monitor.store

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentManageStoreBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.BaseVPAdapter
import com.crayon.fieldapp.ui.screen.monitor.store.listGroup.ListGroupFragment
import com.crayon.fieldapp.ui.screen.monitor.store.listStore.ListStoreFragment
import com.crayon.fieldapp.utils.setSingleClick
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManageStoreFragment :
    BaseFragment<FragmentManageStoreBinding, ManageStoreViewModel>() {

    override val layoutId: Int = R.layout.fragment_manage_store

    override val viewModel: ManageStoreViewModel by viewModel()

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

        tabLayout = view.findViewById(R.id.tab_store)
        viewPager = view.findViewById(R.id.vp_store)

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
        val listGroupFragment = ListGroupFragment()
        val listStoreFragment = ListStoreFragment()
        val bundle = Bundle()
        bundle.putString("agencyId", agencyId)
        listGroupFragment.arguments = bundle
        listStoreFragment.arguments = bundle
        mAdapter.addFragment(listGroupFragment, "Nhóm")
        mAdapter.addFragment(listStoreFragment, "Cửa hàng")

        viewPager.apply {
            offscreenPageLimit = 2
            adapter = mAdapter
        }
        tabLayout.setupWithViewPager(viewPager)
    }
}