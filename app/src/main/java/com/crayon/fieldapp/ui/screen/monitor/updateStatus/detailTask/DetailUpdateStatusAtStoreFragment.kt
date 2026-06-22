package com.crayon.fieldapp.ui.screen.monitor.updateStatus.detailTask

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.databinding.FragmentDetailUpdateStatusAtStoreBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailAttachment.image.ImageAdapter
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaAdapter
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaData
import com.crayon.fieldapp.ui.screen.imageDialog.ImageDialog
import com.crayon.fieldapp.ui.screen.videoDialog.VideoDialog
import com.crayon.fieldapp.utils.formatHour
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.toDate
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import studio.phillip.yolo.utils.TimeFormatUtils

class DetailUpdateStatusAtStoreFragment() :
    BaseFragment<FragmentDetailUpdateStatusAtStoreBinding, DetailUpdateStatusAtStoreViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_detail_update_status_at_store

    private var task: String? = null
    private var taskResponse: TaskResponse? = null

    override val viewModel: DetailUpdateStatusAtStoreViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        task = requireArguments().get("task").toString()
        taskResponse = Gson().fromJson(task, TaskResponse::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.imbIcBack.setSingleClick {
            findNavController().popBackStack()
        }

        taskResponse?.let {
            it.attachments?.let {
                binding.tvNumImage.text = it.size.toString()
                if (it.size > 0) {
                    if (it.size == 1) {
                        it.get(0).createdAt?.toDate("yyyy-MM-dd'T'HH:mm")?.let {
                            binding.txtStartDate.text = TimeFormatUtils.formatHour(it)
                            binding.txtEndTime.text = TimeFormatUtils.formatHour(it)
                        }
                    } else {
                        it.get(0).createdAt?.toDate("yyyy-MM-dd'T'HH:mm")?.let {
                            binding.txtStartDate.text = TimeFormatUtils.formatHour(it)
                        }
                        it.last().createdAt?.toDate("yyyy-MM-dd'T'HH:mm")?.let {
                            binding.txtEndTime.text = TimeFormatUtils.formatHour(it)
                        }
                    }
                }
            }

            binding.txtProjectName.text = it.project!!.name
            it.pic?.let {
                binding.txtPicName.text = it.lastName + " " + it.firstName
            }
            binding.txtShift.text =
                formatHour(it.job!!.startTime.toString()) + "-" + formatHour(it.job!!.endTime.toString())
            binding.txtStatus.text = it.status
            binding.tvTitle.text = it.store!!.name

            var listUrl = ArrayList<MediaData>()
            it.attachments?.forEach {
                if (it.type!!.contains("video")) {
                    listUrl.add(
                        MediaData(
                            it.id,
                            it.thumbnailUrl.toString(),
                            it.imageUrl.toString(),
                            MediaAdapter.MEDIA_VIDEO,
                            it.note,
                            false
                        )
                    )
                } else {
                    listUrl.add(
                        MediaData(
                            it.id,
                            it.thumbnailUrl.toString(),
                            it.imageUrl.toString(),
                            MediaAdapter.MEDIA_IMAGE,
                            it.note,
                            false
                        )
                    )
                }
            }


            val mediaAdapter = ImageAdapter(
                items = listUrl,
                context = requireContext(),
                isIconClose = false,
                removeClickListener = {

                },
                onItemClick = {
                    if (it.type == MediaAdapter.MEDIA_IMAGE) {
                        val imageDialog = ImageDialog(
                            title = it.note ?: "",
                            imageUrl = it.uri
                        )
                        imageDialog.show(childFragmentManager, imageDialog.tag)
                    } else {
                        val videoDialog = VideoDialog(
                            title = it.note ?: "",
                            imageUrl = it.uri
                        )
                        videoDialog.show(childFragmentManager, videoDialog.tag)
                    }

                })
            binding.rvTask.setLayoutManager(GridLayoutManager(requireContext(), 2))
            binding.rvTask.setHasFixedSize(true)
            binding.rvTask.setAdapter(mediaAdapter)
        }

    }

//    private fun toProjectDetail(id: String) {
//        val bundel = bundleOf("projectId" to id, "agencyId" to agencyId)
//        findNavController().navigate(R.id.to_detail_project, bundel)
//    }

}