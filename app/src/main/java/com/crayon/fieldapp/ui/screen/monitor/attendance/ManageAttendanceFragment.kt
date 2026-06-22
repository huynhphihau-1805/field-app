package com.crayon.fieldapp.ui.screen.monitor.attendance

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.model.ProjectStatus
import com.crayon.fieldapp.data.remote.response.TaskType
import com.crayon.fieldapp.databinding.FragmentManageAttendanceBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.BaseVPAdapter
import com.crayon.fieldapp.ui.screen.monitor.listProject.ListProjectFragment
import com.crayon.fieldapp.ui.screen.monitor.listProject.ListProjectViewModel
import com.crayon.fieldapp.utils.setSingleClick
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManageAttendanceFragment :
    BaseFragment<FragmentManageAttendanceBinding, ListProjectViewModel>() {

    override val layoutId: Int = R.layout.fragment_manage_attendance
    override val viewModel: ListProjectViewModel by viewModel()
    lateinit var agencyId: String

    private lateinit var mAdapter: BaseVPAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout


    private var pendingFragment: ListProjectFragment? = null
    private var processingFragment: ListProjectFragment? = null
    private var closedFragment: ListProjectFragment? = null


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
        pendingFragment = ListProjectFragment(ListProjectFragment.FROM_ATTENDANCE_MODE)
        processingFragment = ListProjectFragment(ListProjectFragment.FROM_ATTENDANCE_MODE)
        closedFragment = ListProjectFragment(ListProjectFragment.FROM_ATTENDANCE_MODE)

        val pendingBundle = bundleOf(
            "agencyId" to agencyId,
            "status" to ProjectStatus.Pending,
            "type" to TaskType.TIME_KEEPING.value
        )
        val processingBundle =
            bundleOf(
                "agencyId" to agencyId,
                "status" to ProjectStatus.Processing,
                "type" to TaskType.TIME_KEEPING.value
            )
        val closedBundle = bundleOf(
            "agencyId" to agencyId,
            "status" to ProjectStatus.Closed,
            "type" to TaskType.TIME_KEEPING.value
        )
        pendingFragment?.arguments = pendingBundle
        processingFragment?.arguments = processingBundle
        closedFragment?.arguments = closedBundle

        mAdapter = BaseVPAdapter(childFragmentManager)
        mAdapter.addFragment(processingFragment!!, "Đang chạy")
        mAdapter.addFragment(pendingFragment!!, "Sắp chạy")
        mAdapter.addFragment(closedFragment!!, "Đã đóng")

        viewPager.apply {
            offscreenPageLimit = 3
            adapter = mAdapter
        }
        tabLayout!!.setupWithViewPager(viewPager)
    }

}