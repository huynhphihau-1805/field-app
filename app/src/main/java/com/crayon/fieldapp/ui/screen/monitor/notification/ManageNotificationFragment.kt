package com.crayon.fieldapp.ui.screen.monitor.notification

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentManageNotificationBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.BaseVPAdapter
import com.crayon.fieldapp.ui.screen.monitor.notification.sent.NotificationSentFragment
import com.crayon.fieldapp.utils.setSingleClick
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManageNotificationFragment :
    BaseFragment<FragmentManageNotificationBinding, ManageNotificationViewModel>() {

    override val layoutId: Int = R.layout.fragment_manage_notification

    override val viewModel: ManageNotificationViewModel by viewModel()

    private lateinit var mAdapter: BaseVPAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    lateinit var agencyId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tab_notificaiton)
        viewPager = view.findViewById(R.id.vp_notification)

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
        val sentFragment = NotificationSentFragment()
        val receivedFragment = NotificationSentFragment()
        val bundle = Bundle()
        bundle.putString("agencyId", agencyId)
        sentFragment.arguments = bundle
        receivedFragment.arguments = bundle

        mAdapter.addFragment(sentFragment, "Đã gửi")
        mAdapter.addFragment(receivedFragment, "Đã nhận")

        viewPager.apply {
            offscreenPageLimit = 2
            adapter = mAdapter
        }
        tabLayout!!.setupWithViewPager(viewPager)
    }
}