package com.crayon.fieldapp.ui.screen.detailTask.attendance

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.databinding.FragmentDetailTaskAttendanceBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.dialog.TimeKeepingDialog
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaAdapter
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaData
import com.crayon.fieldapp.ui.screen.imageDialog.ImageDialog
import com.crayon.fieldapp.utils.FileManager
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showMessageDialog
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import studio.phillip.yolo.utils.TaskUtils
import java.io.File

class AttendanceFragment :
    BaseFragment<FragmentDetailTaskAttendanceBinding, TaskAttendanceViewModel>() {

    override val layoutId: Int = R.layout.fragment_detail_task_attendance
    override val viewModel: TaskAttendanceViewModel by viewModel()
    private lateinit var taskId: String
    private lateinit var typeRecord: String
    private var taskResponse: TaskResponse? = null
    private lateinit var mediaAdapter: MediaAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskId = requireArguments().getString("taskId").toString()
        mediaAdapter = MediaAdapter(arrayListOf(), requireContext())
        mediaAdapter.setMediaListener(object : MediaAdapter.MediaListener {
            override fun onItemRemoveClicked(item: MediaData, index: Int) {
                mediaAdapter.removeImage(index)
            }

            override fun onItemClicked(item: MediaData, index: Int) {
                val imageDialog =
                    ImageDialog(imageUrl = item.uri, title = "")
                imageDialog.show(childFragmentManager, imageDialog.tag)
            }

        })
        viewModel.getDetailTask(taskId)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvImages.setLayoutManager(GridLayoutManager(requireContext(), 2))
        binding.rvImages.setHasFixedSize(true)
        binding.rvImages.setAdapter(mediaAdapter)

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.imbAttachment.setSingleClick {
            taskResponse?.let { mTaskResponse ->
                val taskString = Gson().toJson(mTaskResponse).toString()
                val bundle = bundleOf("task" to taskString)
                findNavController().navigate(
                    R.id.action_reportAttendanceFragment_to_detailAttachmentFragment,
                    bundle
                )
            }
        }

        binding.imbIcFilter.setSingleClick {
            if (mediaAdapter.itemCount > 1) {
                context?.showMessageDialog("Bạn chỉ được chụp tối đa 1 tấm")
            } else {
                if (mediaAdapter.itemCount != 0) {
                    mediaAdapter.showProgress(true)
                    taskResponse?.let { mTaskResponse ->
                        viewModel.checkInOut(mTaskResponse, mediaAdapter.getData())
                    }
                } else {
                    context?.showMessageDialog("Bạn chưa có chụp ảnh")
                }
            }
        }

        binding.rlCheckIn.setSingleClick {
            typeRecord = "image"
            taskResponse?.let { mTaskResponse ->
                if (viewModel.verifyLocation(mTaskResponse)) {
                    openCamera()
                } else {
                    val dialog = TimeKeepingDialog()
                    val bundle = Bundle()
                    viewModel.currentLocation?.let {
                        bundle.putDouble("current_lat", it.latitude)
                        bundle.putDouble("current_long", it.longitude)

                    }
                    bundle.putDouble("store_lat", mTaskResponse!!.store!!.lat)
                    bundle.putDouble("store_long", mTaskResponse!!.store!!.lng)
                    bundle.putString("distant", viewModel.strDistant)
                    dialog.arguments = bundle
                    dialog.show(childFragmentManager, dialog.tag)
                }
            }
        }

        viewModel.isEnableCheckIn.observe(viewLifecycleOwner, { isEnable ->
            showButtonCheckIn(isEnable)
        })

        viewModel.isEnableCheckOut.observe(viewLifecycleOwner, { isEnable ->
            showButtonCheckOut(isEnable)
        })

        viewModel.subtitle.observe(viewLifecycleOwner, { subtitle ->
            binding.tvSubTitle.text = subtitle
        })

        binding.rlCheckOut.setSingleClick {
            typeRecord = "image"
            taskResponse?.let { mTaskResponse ->
                if (viewModel.verifyLocation(mTaskResponse)) {
                    openCamera()
                } else {
                    val dialog = TimeKeepingDialog()
                    val bundle = Bundle()
                    viewModel.currentLocation?.let {
                        bundle.putDouble("current_lat", it.latitude)
                        bundle.putDouble("current_long", it.longitude)

                    }
                    bundle.putDouble("store_lat", mTaskResponse!!.store!!.lat)
                    bundle.putDouble("store_long", mTaskResponse!!.store!!.lng)
                    bundle.putString("distant", viewModel.strDistant)
                    dialog.arguments = bundle
                    dialog.show(childFragmentManager, dialog.tag)
                }
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("url")
            ?.observe(viewLifecycleOwner, Observer { result ->
                if (!mediaAdapter.contains(result)) {
                    showImage(result)
                }
            })

        viewModel.task.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        binding.tvTitle.text = it.data!!.type.name
                        taskResponse = it.data!!
                        binding.tvSubTitle.visibility = View.VISIBLE
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.apply {
            updateCheckInOutTask.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        showLoading()
                    }

                    Status.SUCCESS -> {
                        hideLoading()
                        mediaAdapter.clearData()
                        taskResponse?.let { mTaskResponse ->
                            context.showMessageDialog("Cập nhật " + mTaskResponse.type!!.name + " thành công",
                                positiveListener = {
                                    findNavController().navigateUp()
                                })
                        }
                    }

                    Status.ERROR -> {
                        hideLoading()
                    }
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CODE_REQUEST_GALLERY -> {
                    var selectedImageUri = data!!.data
                    val path = FileManager.getPath(requireContext(), selectedImageUri)
                    showImage(path)
                }
            }
        }
    }


    private fun openCamera() {
        val bundle = bundleOf("isTakeImage" to true)
        findNavController().navigate(R.id.action_global_CameraFragment, bundle)
    }

    companion object {
        const val CODE_REQUEST_GALLERY = 1
    }

    private fun showImage(url: String) {
        var type = 0
        if (url.contains("mp4")) {
            type = MediaAdapter.MEDIA_VIDEO
        } else {
            type = MediaAdapter.MEDIA_IMAGE
        }
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                mediaAdapter.addImages(
                    arrayListOf(
                        MediaData(
                            url,
                            File(url).absolutePath,
                            File(url).absolutePath,
                            type,
                            "",
                            false
                        )
                    )
                )
            }
            taskResponse?.let { mTaskResponse ->
                val isHasTag = TaskUtils.isHasTag(mTaskResponse.type!!.id!!.toInt())
                val size = TaskUtils.getQualityOfImage(mTaskResponse.type!!.id!!.toInt())
                val file = viewModel.createFile(url, mTaskResponse, size, isHasTag)
                file?.let { myFile ->
                    withContext(Dispatchers.Main) {
                        mediaAdapter.updateImage(
                            MediaData(
                                url,
                                myFile.absolutePath,
                                myFile.absolutePath,
                                type,
                                "",
                                true
                            )
                        )
                    }
                }
            }
        }
    }

    private fun showButtonCheckIn(isShow: Boolean) {
        if (isShow) {
            binding.rlCheckIn.isEnabled = true
            binding.imgCheckIn.setColorFilter(
                resources.getColor(
                    R.color.colorRed,
                    null
                )
            )
            binding.txtCheckIn.setTextColor(
                resources.getColor(
                    R.color.colorBlack,
                    null
                )
            )
        } else {
            binding.rlCheckIn.isEnabled = false
            binding.imgCheckIn.setColorFilter(
                resources.getColor(
                    R.color.colorGray,
                    null
                )
            )
            binding.txtCheckIn.setTextColor(
                resources.getColor(
                    R.color.colorGray,
                    null
                )
            )
        }
    }

    private fun showButtonCheckOut(isShow: Boolean) {
        if (isShow) {
            binding.rlCheckOut.isEnabled = true
            binding.imgCheckOut.setColorFilter(
                resources.getColor(
                    R.color.colorRed,
                    null
                )
            )
            binding.txtCheckOut.setTextColor(
                resources.getColor(
                    R.color.colorBlack,
                    null
                )
            )
        } else {
            binding.rlCheckOut.isEnabled = false
            binding.imgCheckOut.setColorFilter(
                resources.getColor(
                    R.color.colorGray,
                    null
                )
            )
            binding.txtCheckOut.setTextColor(
                resources.getColor(
                    R.color.colorGray,
                    null
                )
            )
        }
    }
}