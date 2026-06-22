package com.crayon.fieldapp.ui.screen.detailAttachment.image

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.data.remote.response.TaskType
import com.crayon.fieldapp.databinding.FragmentListGroupBinding
import com.crayon.fieldapp.databinding.FragmentListImageBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaAdapter
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaData
import com.crayon.fieldapp.ui.screen.imageDialog.ImageDialog
import com.crayon.fieldapp.ui.screen.videoDialog.VideoDialog
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.showConfirmDialog
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListImageFragment : BaseFragment<FragmentListImageBinding, ListImageViewModel>() {

    override val layoutId: Int = R.layout.fragment_list_image
    override val viewModel: ListImageViewModel by viewModel()

    private lateinit var mediaAdapter: ImageAdapter
    private var task: TaskResponse? = null
    private var type: String? = null
    private var imageId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val taskString = requireArguments().get("task").toString()
        type = requireArguments().get("type").toString()
        task = Gson().fromJson(taskString, TaskResponse::class.java)
        var isShowIconClose = true
        task?.let {
            if (it.type.identifier == TaskType.TIME_KEEPING.value) {
                isShowIconClose = false
            } else {
                isShowIconClose = true
            }
        }

        mediaAdapter =
            ImageAdapter(items = arrayListOf(), isIconClose = isShowIconClose, onItemClick = {
                if (it.type == ImageAdapter.MEDIA_IMAGE) {
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
            }, removeClickListener = {
                context.showConfirmDialog(
                    title = "Bạn có muốn xoá ảnh này không?",
                    textPositive = "Có",
                    textNegative = "Không",
                    positiveListener = {
                        imageId = it?.id.toString()
                        viewModel.removeImage(task?.id.toString(), arrayListOf(imageId.toString()))
                    }
                )
            }, context = requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var listUrl = ArrayList<MediaData>()
        if (type.equals("video")) {
            task?.attachments?.forEach {
                if (it.type!!.contains("video")) {
                    val item = MediaData(
                        it.id,
                        it.thumbnailUrl.toString(),
                        it.imageUrl.toString(),
                        MediaAdapter.MEDIA_VIDEO,
                        it.note,
                        false
                    )
                    listUrl.add(item)
                }
            }
        } else {
            task?.attachments?.forEach {
                if (it.type!!.contains("image")) {
                    val item = MediaData(
                        it.id,
                        it.thumbnailUrl.toString(),
                        it.imageUrl.toString(),
                        MediaAdapter.MEDIA_IMAGE,
                        it.note,
                        false
                    )
                    listUrl.add(item)
                }
            }
        }
        mediaAdapter.addImages(
            listUrl
        )

        binding.rvImages.setLayoutManager(GridLayoutManager(requireContext(), 2))
        binding.rvImages.setHasFixedSize(true)
        binding.rvImages.setAdapter(mediaAdapter)

        viewModel.apply {
            isRemoveTask.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        mediaAdapter.showProgress(false)
                        showLoading()
                    }
                    Status.SUCCESS -> {
                        hideLoading()
                        mediaAdapter.deleteImage(imageId.toString())
                    }
                    Status.ERROR -> {
                        hideLoading()
                    }
                }
            })
        }

    }
}