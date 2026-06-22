package com.crayon.fieldapp.ui.screen.monitor

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.model.GridMenu
import com.crayon.fieldapp.data.remote.response.RoleResponse
import com.crayon.fieldapp.databinding.FragmentMonitorBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.MenuRVAdapter
import com.crayon.fieldapp.ui.base.dialog.selectAgency.SelectAgencyDialog
import com.crayon.fieldapp.ui.widgets.GridDividerItemDecoration
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import org.koin.androidx.viewmodel.ext.android.viewModel

class MonitorFragment : BaseFragment<FragmentMonitorBinding, MonitorViewModel>(),
    SelectAgencyDialog.SelectAgencyListener,
    MenuRVAdapter.MenuListener {

    override val layoutId: Int = R.layout.fragment_monitor

    override val viewModel: MonitorViewModel by viewModel()

    private lateinit var adapter: MenuRVAdapter
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private var agneycId: String? = null
    private var mListAgency = ArrayList<RoleResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getListRole()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inital()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.imbIcFilter.setSingleClick {
            val agencyDialog = SelectAgencyDialog()
            agencyDialog.setListener(this@MonitorFragment)
            val bundle = Bundle()
            bundle.putSerializable("agency", mListAgency)
            agencyDialog.arguments = bundle
            agencyDialog.show(childFragmentManager, agencyDialog.tag)
        }

        viewModel.apply {
            listAgency.observe(viewLifecycleOwner, Observer { event ->
                event.getContentIfNotHandled().let {
                    it?.let {
                        when (it.status) {
                            Status.LOADING -> {
                                binding.pbLoading.visibility = View.VISIBLE
                            }

                            Status.SUCCESS -> {
                                binding.pbLoading.visibility = View.GONE
                                it.data?.let { items ->
                                    mListAgency.clear()
                                    mListAgency.addAll(items)
                                    if (items.size == 0) {
                                        findNavController().navigateUp()
                                    } else if (items.size == 1) {
                                        binding.tvSubTitle.text = items.get(0).agency.name
                                        agneycId = items.get(0).agency.id
                                    } else {
                                        var listAgency = ArrayList<RoleResponse>()
                                        listAgency = items as ArrayList<RoleResponse>
                                        val agencyDialog = SelectAgencyDialog()
                                        agencyDialog.setListener(this@MonitorFragment)
                                        val bundle = Bundle()
                                        bundle.putSerializable("agency", listAgency)
                                        agencyDialog.arguments = bundle
                                        agencyDialog.show(childFragmentManager, agencyDialog.tag)
                                    }
                                }

                            }

                            Status.ERROR -> {
                                binding.pbLoading.visibility = View.GONE
                            }
                        }
                    }

                }
            })
        }
    }

    private fun inital() {
        var menu: ArrayList<GridMenu> = ArrayList()
        menu.add(
            GridMenu(
                TIME_KEEPING,
                R.drawable.ic_white_check_in,
                resources.getString(R.string.txt_timekeeping),
                R.color.bgCheckIn
            )
        )
        menu.add(
            GridMenu(
                UPDATE_STATUS,
                R.drawable.ic_gray_time_line,
                resources.getString(R.string.txt_update_status),
                R.color.bgCalendar
            )
        )
        menu.add(
            GridMenu(
                REPORT_SALES,
                R.drawable.icon_sales,
                resources.getString(R.string.txt_sales),
                R.color.bgReportSales
            )
        )
        menu.add(
            GridMenu(
                REPORT_COMPITETOR,
                R.drawable.icon_competitor,
                resources.getString(R.string.txt_competitor),
                R.color.bgReportCompetitor
            )
        )
//        menu.add(
//            GridMenu(
//                TRACKING,
//                R.drawable.icon_map,
//                resources.getString(R.string.txt_tracking),
//                R.color.bgTracking
//            )
//        )
        menu.add(
            GridMenu(
                CHANGE_GIFT,
                R.drawable.ic_gift,
                resources.getString(R.string.txt_change_gift),
                R.color.bgChangeGift
            )
        )
        menu.add(
            GridMenu(
                EMPLOYEE_AGENCY,
                R.drawable.ic_white_people,
                resources.getString(R.string.txt_employee),
                R.color.bgCalendar
            )
        )
        menu.add(
            GridMenu(
                APPLICATION,
                R.drawable.ic_menu,
                resources.getString(R.string.txt_application),
                R.color.bgMonitoring
            )
        )
        menu.add(
            GridMenu(
                NOTIFICAITON,
                R.drawable.ic_white_notification,
                resources.getString(R.string.txt_notification),
                R.color.bgNotification
            )
        )
        menu.add(
            GridMenu(
                STORE,
                R.drawable.ic_white_shopping_cart,
                resources.getString(R.string.txt_store),
                R.color.bgStore
            )
        )
        menu.add(
            GridMenu(
                PROJECT,
                R.drawable.ic_white_my_job,
                resources.getString(R.string.txt_project),
                R.color.bgReportProject
            )
        )

        adapter = MenuRVAdapter(menu, requireContext(), this)
        mLayoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)
        binding.rvMenu.setLayoutManager(mLayoutManager)
        binding.rvMenu.addItemDecoration(
            GridDividerItemDecoration(
                requireContext(),
                R.drawable.line_divider_white
            )
        )
        binding.rvMenu.adapter = adapter
    }

    companion object {
        val TIME_KEEPING: Int = 0
        val UPDATE_STATUS: Int = 1
        val EMPLOYEE_AGENCY: Int = 2
        val APPLICATION: Int = 3
        val NOTIFICAITON: Int = 4
        val STORE: Int = 5
        val PROJECT: Int = 6
        val REPORT_SALES: Int = 7
        val REPORT_COMPITETOR: Int = 8
        val TRACKING: Int = 9
        val CHANGE_GIFT: Int = 10
    }

    override fun onMenuItemClicked(id: Int) {
        val bundle = bundleOf("agencyId" to agneycId)
        when (id) {
            TIME_KEEPING -> {
                findNavController().navigate(
                    R.id.action_monitorFragment_to_manageAttendanceFragment,
                    bundle
                )
            }

            UPDATE_STATUS -> {
                findNavController().navigate(
                    R.id.action_monitorFragment_to_manageUpdateStatusFragment,
                    bundle
                )
            }

            REPORT_COMPITETOR -> {
                findNavController().navigate(
                    R.id.action_monitorFragment_to_manageReportCompetitorFragment,
                    bundle
                )
            }

            TRACKING -> {
                findNavController().navigate(
                    R.id.action_monitorFragment_to_manageReportTrackingFragment,
                    bundle
                )
            }

            CHANGE_GIFT -> {
                findNavController().navigate(
                    R.id.action_monitorFragment_to_manageChangeGiftFragment,
                    bundle
                )
            }

            REPORT_SALES -> {
                findNavController().navigate(
                    R.id.action_monitorFragment_to_manageReportSalesFragment,
                    bundle
                )
            }

            EMPLOYEE_AGENCY -> {
                findNavController().navigate(
                    R.id.action_monitorFragment_to_manageEmployeeFragment,
                    bundle
                )
            }

            APPLICATION -> {
                findNavController().navigate(
                    R.id.action_monitorFragment_to_manageApplicationFragment,
                    bundle
                )
            }

            NOTIFICAITON -> {
                findNavController().navigate(
                    R.id.action_monitorFragment_to_manageNotificationFragment,
                    bundle
                )
            }

            STORE -> {
                findNavController().navigate(
                    R.id.action_monitorFragment_to_manageStoreFragment,
                    bundle
                )
            }

            PROJECT -> {
                findNavController().navigate(
                    R.id.action_monitorFragment_to_manageProjectFragment,
                    bundle
                )
            }
        }
    }

    override fun onSelectAgencyItemClicked(role: RoleResponse) {
        this.agneycId = role.agency.id
        binding.tvSubTitle.text = role.agency.name
    }

}