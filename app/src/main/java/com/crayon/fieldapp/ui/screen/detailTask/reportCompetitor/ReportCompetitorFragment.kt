package com.crayon.fieldapp.ui.screen.detailTask.reportCompetitor

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.JobResponse
import com.crayon.fieldapp.databinding.FragmentReportCompetitorBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.dialog.TimeKeepingDialog
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaAdapter
import com.crayon.fieldapp.ui.screen.detailTask.reportCompetitor.adapter.ReportCompetitorRVAdapter
import com.crayon.fieldapp.ui.screen.imageDialog.ImageDialog
import com.crayon.fieldapp.ui.screen.videoDialog.VideoDialog
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.formatStartEndFullDate
import com.crayon.fieldapp.utils.setSingleClick
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReportCompetitorFragment :
    BaseFragment<FragmentReportCompetitorBinding, ReportCompetitorViewModel>() {

    override val layoutId: Int = R.layout.fragment_report_competitor
    override val viewModel: ReportCompetitorViewModel by viewModel()
    private lateinit var mCompetitorAdapter: ReportCompetitorRVAdapter
    private lateinit var taskId: String
    private var jobJson: String? = null
    private var jobResponse: JobResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskId = requireArguments().getString("taskId").toString()
        jobJson = requireArguments().getString("job").toString()
        jobJson?.let {
            jobResponse = Gson().fromJson(it, JobResponse::class.java)
        }

        mCompetitorAdapter =
            ReportCompetitorRVAdapter(arrayListOf(), requireContext(), {
                // Item
            }, {
                // Image
                if (it.type == MediaAdapter.MEDIA_IMAGE) {
                    val imageDialog = ImageDialog(
                        title = "",
                        imageUrl = it.uri
                    )
                    imageDialog.show(childFragmentManager, imageDialog.tag)
                } else {
                    val imageDialog = VideoDialog(
                        title = "",
                        imageUrl = it.uri
                    )
                    imageDialog.show(childFragmentManager, imageDialog.tag)
                }

            })

        viewModel.getReportOpponents(taskId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        jobResponse?.let {
            it.project?.let {
                binding.txtProjectName.text = it.name.toString()
            }
            it.store?.let {
                binding.txtAddress.text = it.address.toString()
            }

            if (it.startTime != null && it.endTime != null) {
                binding.txtTime.text = formatStartEndFullDate(it.startTime!!, it.endTime!!)
            }
            it.status?.let {
                if (it.equals("Processing")) {
                    binding.txtStaus.text = "Đang chạy"
                    binding.txtStaus.setTextColor(
                        requireContext().resources.getColor(
                            R.color.colorAccent,
                            null
                        )
                    )
                } else {
                    binding.txtStaus.text = "Đã đóng"
                    binding.txtStaus.setTextColor(
                        requireContext().resources.getColor(
                            R.color.colorGray,
                            null
                        )
                    )
                }
            }
        }

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.btnAddCustomer.setSingleClick {
            jobResponse?.store?.let { store ->
                if (viewModel.verifyLocation(store)) {
                    findNavController().navigate(
                        R.id.action_reportCompetitorFragment_to_addReportCompetitorFragment,
                        bundleOf("taskId" to taskId.toString())
                    )
                } else {
                    val dialog = TimeKeepingDialog()
                    val bundle = Bundle()
                    viewModel.currentLocation?.let {
                        bundle.putDouble("current_lat", it.latitude)
                        bundle.putDouble("current_long", it.longitude)

                    }
                    bundle.putDouble("store_lat", store.lat ?: 0.0)
                    bundle.putDouble("store_long", store.lng ?: 0.0)
                    bundle.putString("distant", viewModel.strDistant)
                    dialog.arguments = bundle
                    dialog.show(childFragmentManager, dialog.tag)
                }
            }
        }

        binding.rvCustomer.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = mCompetitorAdapter
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("isNew")
            ?.observe(viewLifecycleOwner, Observer { isNew ->
                if (isNew) {
                    taskId?.let {
                        viewModel.getReportOpponents(it)
                    }
                }
            })


        viewModel.reportOpponents.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            val items = it.data
                            items?.let {
                                if (items.size > 0) {
                                    binding.rlEmpty.visibility = View.GONE
                                    binding.rvCustomer.visibility = View.VISIBLE
                                    mCompetitorAdapter.addItems(items)
                                } else {
                                    binding.rlEmpty.visibility = View.VISIBLE
                                    binding.rvCustomer.visibility = View.GONE
                                }
                            }
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })
    }
}