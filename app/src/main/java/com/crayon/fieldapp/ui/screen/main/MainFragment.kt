package com.crayon.fieldapp.ui.screen.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Observer
import com.crayon.fieldapp.BuildConfig
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentMainBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.BaseVPAdapter
import com.crayon.fieldapp.ui.screen.home.HomeFragment
import com.crayon.fieldapp.ui.screen.notification.NotificationFragment
import com.crayon.fieldapp.ui.screen.profile.ProfileFragment
import com.crayon.fieldapp.utils.Status
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainFragment : BaseFragment<FragmentMainBinding, MainViewModel>() {
    override val layoutId: Int = R.layout.fragment_main
    override val viewModel: MainViewModel by viewModel()
    lateinit var mViewPagerAdapter: BaseVPAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        binding.tabDashboard.setupWithViewPager(binding.vpDashboard)
        setupTablayout()

        viewModel.apply {
            fetchCurrentLocation()
            isUpdateVersion.observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let {
                    when (it.status) {
                        Status.LOADING -> {

                        }

                        Status.SUCCESS -> {
                            val data = it.data
                            data?.let {
                                val androidVersion =
                                    it.findLast { it.key.equals("android_app_version") }
                                androidVersion?.let {
                                    if (!it.value.equals(BuildConfig.VERSION_NAME)) {
//                                        requireContext().showDialog(
//                                            title = "Thông báo",
//                                            message = "Vui lòng cập phiên bản mới: " + it.value,
//                                            positiveListener = {
//                                                val browserIntent = Intent(
//                                                    Intent.ACTION_VIEW,
//                                                    Uri.parse("https://play.google.com/store/apps/details?id=com.crayon.fieldapp")
//                                                )
//                                                startActivity(browserIntent)
//                                            }
//                                        )
                                    }
                                }
                            }
                        }

                        Status.ERROR -> {

                        }
                    }
                }

            })
            checkVersionApp()
        }
    }

    private fun setupViewPager() {
        mViewPagerAdapter = BaseVPAdapter(childFragmentManager)
        mViewPagerAdapter.addFragment(HomeFragment(), "")
        mViewPagerAdapter.addFragment(NotificationFragment(), "")
        mViewPagerAdapter.addFragment(ProfileFragment(), "")

        binding.vpDashboard.apply {
            offscreenPageLimit = 4
            adapter = mViewPagerAdapter
        }
    }

    private fun setupTablayout() {
        val icProfile = LayoutInflater.from(requireContext())
            .inflate(R.layout.custom_main_tab, null) as ImageView
        val icNotification = LayoutInflater.from(requireContext())
            .inflate(R.layout.custom_main_tab, null) as ImageView
        val icHome = LayoutInflater.from(requireContext())
            .inflate(R.layout.custom_main_tab, null) as ImageView

        icProfile.setImageResource(R.drawable.ic_gray_profile)
        icNotification.setImageResource(R.drawable.ic_gray_notification)
        icHome.setImageResource(R.drawable.ic_gray_home)

        binding.tabDashboard.getTabAt(0)!!.customView = icHome
        binding.tabDashboard.getTabAt(1)!!.customView = icNotification
        binding.tabDashboard.getTabAt(2)!!.customView = icProfile

        // Set color selected
        icHome.setImageResource(R.drawable.ic_blue_home)
        binding.tabDashboard.setSelectedTabIndicatorColor(
            getColor(
                requireContext(),
                R.color.colorPrimary
            )
        )
        binding.tabDashboard.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                icHome.setImageResource(if (binding.tabDashboard.getTabAt(0)!!.isSelected) R.drawable.ic_blue_home else R.drawable.ic_gray_home)
                icNotification.setImageResource(if (binding.tabDashboard.getTabAt(1)!!.isSelected) R.drawable.ic_blue_notification else R.drawable.ic_gray_notification)
                icProfile.setImageResource(if (binding.tabDashboard.getTabAt(2)!!.isSelected) R.drawable.ic_blue_profile else R.drawable.ic_gray_profile)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }


}