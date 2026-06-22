package com.crayon.fieldapp.ui.screen.monitor.attendance.detailTask

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.databinding.FragmentDetailAttendanceAtStoreBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailAttachment.image.ImageAdapter
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaAdapter
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaData
import com.crayon.fieldapp.ui.screen.imageDialog.ImageDialog
import com.crayon.fieldapp.utils.formatHour
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.toDate
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import studio.phillip.yolo.utils.TimeFormatUtils

class DetailAttendanceAtStoreFragment() :
    BaseFragment<FragmentDetailAttendanceAtStoreBinding, DetailAttendacneAtStoreViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_detail_attendance_at_store

    private var task: String? = null
    private var taskResponse: TaskResponse? = null

    override val viewModel: DetailAttendacneAtStoreViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        task = requireArguments().get("task").toString()
        taskResponse = Gson().fromJson(task, TaskResponse::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imbIcBack.setSingleClick {
            findNavController().popBackStack()
        }

        taskResponse?.let {
            it.project?.let { project ->
                binding.txtProjectName.text = project.name
            }
            it.pic?.let { pic ->
                binding.txtPicName.text = pic.lastName + " " + pic.firstName
            }
            it.job?.let { job ->
                binding.txtShift.text =
                    formatHour(job.startTime.toString()) + "-" + formatHour(job.endTime.toString())
            }
            binding.txtStatus.text = it.status
            it.store?.let { store ->
                binding.tvTitle.text = store.name
            }
            var listUrl = ArrayList<MediaData>()
            it.attendances?.let { attendances ->
                if (attendances.size == 1) {
                    attendances.get(0).checkInTime?.toDate("yyyy-MM-dd'T'HH:mm")?.let {
                        binding.txtStartDate.text = TimeFormatUtils.formatFullDate(it)
                    }

                    attendances.get(0).checkOutTime?.toDate("yyyy-MM-dd'T'HH:mm")?.let {
                        binding.txtEndTime.text = TimeFormatUtils.formatFullDate(it)
                    }
                }
            }

            it.attachments?.forEach {
                if (it.type!!.contains("video")) {
                    listUrl.add(
                        MediaData(
                            it.id,
                            it.thumbnailUrl.toString(),
                            it.imageUrl.toString(),
                            MediaAdapter.MEDIA_VIDEO,
                            "",
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
                            "",
                            false
                        )
                    )
                }
            }


            val mediaAdapter = ImageAdapter(items = listUrl,
                isIconClose = false, onItemClick = {
                    val imageDialog = ImageDialog(
                        title = "",
                        imageUrl = it.uri
                    )
                    imageDialog.show(childFragmentManager, imageDialog.tag)
                }, removeClickListener = {

                }, context = requireContext()
            )
            binding.rvTask.setLayoutManager(GridLayoutManager(requireContext(), 2))
            binding.rvTask.setHasFixedSize(true)
            binding.rvTask.setAdapter(mediaAdapter)
        }
    }
}