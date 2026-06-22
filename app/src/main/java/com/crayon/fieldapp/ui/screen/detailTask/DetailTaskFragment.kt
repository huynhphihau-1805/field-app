package com.crayon.fieldapp.ui.screen.detailTask

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.data.remote.response.TaskType
import com.crayon.fieldapp.databinding.FragmentDetailTaskBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.dialog.TimeKeepingDialog
import com.crayon.fieldapp.ui.base.dialog.getPhoto.GetPhotoDialogFragment
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaAdapter
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaData
import com.crayon.fieldapp.ui.screen.detailTask.base.DetailTaskViewModel
import com.crayon.fieldapp.ui.screen.imageDialog.EditNoteDialog
import com.crayon.fieldapp.utils.BitmapUtils
import com.crayon.fieldapp.utils.FileManager
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.loadImage
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

class DetailTaskFragment :
    BaseFragment<FragmentDetailTaskBinding, DetailTaskViewModel>(),
    GetPhotoDialogFragment.GetPhotoDialogListener {

    override val layoutId: Int = R.layout.fragment_detail_task
    override val viewModel: DetailTaskViewModel by viewModel()
    private lateinit var taskId: String
    private lateinit var typeRecord: String
    private var taskResponse: TaskResponse? = null
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskId = requireArguments().getString("taskId").toString()
        mediaAdapter = MediaAdapter(arrayListOf(), requireContext())
        mediaAdapter.setMediaListener(object : MediaAdapter.MediaListener {
            override fun onItemRemoveClicked(item: MediaData, index: Int) {
                mediaAdapter.removeImage(index)
            }

            override fun onItemClicked(item: MediaData, index: Int) {
                val editNote = EditNoteDialog(
                    index = index,
                    note = "",
                    imageUrl = item.uri
                )
                editNote.setEditNoteListener(object : EditNoteDialog.EditNoteListener {
                    override fun onEditNote(note: String, index: Int) {
                        mediaAdapter.updateNote(note, index)
                    }
                })
                editNote.show(childFragmentManager, editNote.tag)
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
                    R.id.action_detailTaskFragment_to_detailAttachmentFragment,
                    bundle
                )
            }

        }

        binding.imbIcFilter.setSingleClick {
            if (mediaAdapter.itemCount > 1) {
                context?.showMessageDialog("Mỗi lần chỉ được upload 1 ảnh/Video")
            } else {
                if (mediaAdapter.itemCount != 0) {
                    mediaAdapter.showProgress(true)
                    val listNote = mediaAdapter.getNotes()
                    taskResponse?.let { mTaskResponse ->
                        viewModel.upLoadTask(mTaskResponse, mediaAdapter.getData(), listNote)
                    }
                } else {
                    context?.showMessageDialog("Bạn chưa có chụp ảnh")
                }
            }
        }

        binding.imgPicture.setSingleClick {
            typeRecord = "image"
            taskResponse?.let { mTaskResponse ->
                val taskType = mTaskResponse.type.id!!.toInt()
                if (isVerifyLocation(taskType)) {
                    if (viewModel.verifyLocation(mTaskResponse)) {
                        // TODO
                        if (isTakeImageOnlyFromCamera(taskType)) {
                            openCamera()
                        } else {
                            openCamera()
                        }
                    } else {
                        val dialog = TimeKeepingDialog()
                        val bundle = Bundle()
                        viewModel.currentLocation.let {
                            bundle.putDouble("current_lat", it.latitude)
                            bundle.putDouble("current_long", it.longitude)

                        }
                        bundle.putDouble("store_lat", mTaskResponse.store!!.lat)
                        bundle.putDouble("store_long", mTaskResponse.store!!.lng)
                        bundle.putString("distant", viewModel.strDistant)
                        dialog.arguments = bundle
                        dialog.show(childFragmentManager, dialog.tag)
                    }

                } else {
                    // TODO
                    if (isTakeImageOnlyFromCamera(taskType)) {
                        openCamera()
                    } else {
                        openCamera()
                    }
                }
            }

        }

        binding.imgVideo.setSingleClick {
            typeRecord = "video"
            taskResponse?.let { mTaskResponse ->
                val taskType = mTaskResponse.type!!.id!!.toInt()
                if (isVerifyLocation(taskType)) {
                    if (viewModel.verifyLocation(mTaskResponse)) {
                        openVideoCamera()
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
                } else {
                    openVideoCamera()
                }
            }

        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("url")
            ?.observe(viewLifecycleOwner, Observer { result ->
                if (!mediaAdapter.contains(result)) {
                    showImage(result)
                }
            })

        viewModel.title.observe(viewLifecycleOwner, { data ->
            binding.tvTitle.text = data
        })

        viewModel.isRecordVideo.observe(viewLifecycleOwner, { isShow ->
            if(isShow){
                binding.imgVideo.visibility = View.VISIBLE
            } else {
                binding.imgVideo.visibility = View.GONE
            }
        })

        viewModel.isRecordImage.observe(viewLifecycleOwner, { isShow ->
            if(isShow){
                binding.imgPicture.visibility = View.VISIBLE
            } else {
                binding.imgPicture.visibility = View.GONE
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
                        taskResponse = it.data!!
                        taskResponse?.let { mTaskResponse ->
                            showSubTitle(mTaskResponse.type!!.id!!.toInt())
                        }
                    }
                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.apply {
            updateTask.observe(viewLifecycleOwner, Observer {
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

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val selectedImageUri = data?.data
                val path = FileManager.getPath(requireContext(), selectedImageUri)
                showImage(path)
            }
        }

    }

    private fun isTakeImageOnlyFromCamera(taskType: Int): Boolean {
        when (taskType) {
            TaskType.REPORT_CUSTOMER.value,
            TaskType.REPORT_END_SHIFT.value,
            TaskType.COUNT.value -> {
                return false
            }
            TaskType.UPDATE_STATUS.value,
            TaskType.CHECK_LIST.value,
            TaskType.VISIT_STORE.value,
            TaskType.SET_UP.value,
            TaskType.REPORT_COMPITETOR.value,
            TaskType.REPORT_DAMAGED.value,
            TaskType.CLEAN_UP.value,
            TaskType.REPORT_VIOLATION.value,
            TaskType.UPDATE_PRICE.value,
            TaskType.COMPLETE_FIX.value,
            TaskType.TIME_KEEPING.value -> {
                return true
            }
            else -> {
                return false
            }
        }
    }

    private fun isVerifyLocation(taskType: Int): Boolean {
        when (taskType) {
            TaskType.REPORT_CUSTOMER.value,
            TaskType.CHECK_LIST.value -> {
                return false
            }
            TaskType.REPORT_END_SHIFT.value,
            TaskType.COUNT.value,
            TaskType.UPDATE_STATUS.value,
            TaskType.VISIT_STORE.value,
            TaskType.SET_UP.value,
            TaskType.REPORT_COMPITETOR.value,
            TaskType.REPORT_DAMAGED.value,
            TaskType.CLEAN_UP.value,
            TaskType.REPORT_VIOLATION.value,
            TaskType.UPDATE_PRICE.value,
            TaskType.COMPLETE_FIX.value,
            TaskType.TIME_KEEPING.value -> {
                return true
            }
        }
        return false
    }

    private fun showSubTitle(taskType: Int) {
        when (taskType) {
            TaskType.CHECK_LIST.value,
            TaskType.VISIT_STORE.value,
            TaskType.REPORT_CUSTOMER.value,
            TaskType.SET_UP.value,
            TaskType.UPDATE_STATUS.value,
            TaskType.REPORT_COMPITETOR.value,
            TaskType.REPORT_DAMAGED.value,
            TaskType.CLEAN_UP.value,
            TaskType.REPORT_VIOLATION.value,
            TaskType.REPORT_END_SHIFT.value,
            TaskType.COUNT.value,
            TaskType.UPDATE_PRICE.value,
            TaskType.COMPLETE_FIX.value -> {
                binding.tvSubTitle.visibility = View.GONE
            }
            TaskType.TIME_KEEPING.value -> {
                binding.tvSubTitle.visibility = View.VISIBLE
                binding.tvSubTitle.text = "Check In"
                taskResponse?.let {
                    it.attendances?.let { attendances ->
                        if (attendances.size == 0) {
                            binding.tvSubTitle.text = "Check In"
                        } else if (attendances.size == 1) {
                            binding.tvSubTitle.text = "Check Out"
                        } else {
                            binding.tvSubTitle.text = "Đã chấm công"
                        }
                    }
                }
            }
        }
    }


    override fun selectFromGallery() {
        openGallery()
    }

    override fun selectFromCamera() {
        openCamera()
    }

    private fun openCamera() {
        val bundle = bundleOf("isTakeImage" to true)
        findNavController().navigate(R.id.action_global_CameraFragment, bundle)
    }

    private fun openVideoCamera() {
        val bundle = bundleOf("isTakeImage" to false)
        findNavController().navigate(R.id.action_global_CameraFragment, bundle)
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        val chooserIntent = Intent.createChooser(intent, "Select Picture")
        galleryLauncher.launch(chooserIntent)
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
}