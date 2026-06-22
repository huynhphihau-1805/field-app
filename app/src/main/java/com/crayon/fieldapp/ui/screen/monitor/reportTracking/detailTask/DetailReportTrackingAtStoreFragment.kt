package com.crayon.fieldapp.ui.screen.monitor.reportTracking.detailTask

import android.os.Bundle
import android.view.View
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.databinding.FragmentDetailReportTrackingAtStoreBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailReportTrackingAtStoreFragment() :
    BaseFragment<FragmentDetailReportTrackingAtStoreBinding, DetailReportTrackingAtStoreViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_detail_report_tracking_at_store

    private var task: String? = null
    private var taskResponse: TaskResponse? = null

    override val viewModel: DetailReportTrackingAtStoreViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        taskResponse = Gson().fromJson(task, TaskResponse::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.imbIcBack.setSingleClick {
//            findNavController().popBackStack()
//        }
//
//        taskResponse?.let {
//            it.attachments?.let {
//                binding.tvNumImage.text = it.size.toString()
//                if (it.size > 0) {
//                    if (it.size == 1) {
//                        it.get(0).createdAt?.toDate("yyyy-MM-dd'T'HH:mm")?.let {
//                            txt_start_date.text = TimeFormatUtils.formatHour(it)
//                            txt_end_time.text = TimeFormatUtils.formatHour(it)
//                        }
//                    } else {
//                        it.get(0).createdAt?.toDate("yyyy-MM-dd'T'HH:mm")?.let {
//                            txt_start_date.text = TimeFormatUtils.formatHour(it)
//                        }
//                        it.last().createdAt?.toDate("yyyy-MM-dd'T'HH:mm")?.let {
//                            txt_end_time.text = TimeFormatUtils.formatHour(it)
//                        }
//                    }
//                }
//            }
//
//            binding.txt_project_name.text = it.project!!.name
//            it.pic?.let {
//                txt_pic_name.text = it.lastName + " " + it.firstName
//            }
//            txt_shift.text =
//                formatHour(it.job!!.startTime.toString()) + "-" + formatHour(it.job!!.endTime.toString())
//            txt_status.text = it.status
//            tv_title.text = it.store!!.name
//
//            var listUrl = ArrayList<MediaData>()
//            it.attachments?.forEach {
//                if (it.type!!.contains("video")) {
//                    listUrl.add(
//                        MediaData(
//                            it.id,
//                            it.thumbnailUrl.toString(),
//                            it.imageUrl.toString(),
//                            MediaAdapter.MEDIA_VIDEO,
//                            it.note,
//                            false
//                        )
//                    )
//                } else {
//                    listUrl.add(
//                        MediaData(
//                            it.id,
//                            it.thumbnailUrl.toString(),
//                            it.imageUrl.toString(),
//                            MediaAdapter.MEDIA_IMAGE,
//                            it.note,
//                            false
//                        )
//                    )
//                }
//            }
//
//
//            val mediaAdapter = ImageAdapter(
//                items = listUrl,
//                context = requireContext(),
//                isIconClose = false,
//                removeClickListener = {
//
//                },
//                onItemClick = {
//                    if (it.type == MediaAdapter.MEDIA_IMAGE) {
//                        val imageDialog = ImageDialog(
//                            title = it.note ?: "",
//                            imageUrl = it.uri
//                        )
//                        imageDialog.show(childFragmentManager, imageDialog.tag)
//                    } else {
//                        val videoDialog = VideoDialog(
//                            title = it.note ?: "",
//                            imageUrl = it.uri
//                        )
//                        videoDialog.show(childFragmentManager, videoDialog.tag)
//                    }
//
//                })
//            rv_task.setLayoutManager(GridLayoutManager(requireContext(), 2))
//            rv_task.setHasFixedSize(true)
//            rv_task.setAdapter(mediaAdapter)
    }
}

//    private fun toProjectDetail(id: String) {
//        val bundel = bundleOf("projectId" to id, "agencyId" to agencyId)
//        findNavController().navigate(R.id.to_detail_project, bundel)
//    }
