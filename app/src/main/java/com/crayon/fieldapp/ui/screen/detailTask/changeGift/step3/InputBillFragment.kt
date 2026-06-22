package com.crayon.fieldapp.ui.screen.detailTask.changeGift.step3

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.CreateCustomerBillResponse
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.databinding.FragmentInputBillBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailAttachment.image.ImageAdapter
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaAdapter
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaData
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.step3.adapter.UploadMediaAdapter
import com.crayon.fieldapp.ui.screen.imageDialog.ImageDialog
import com.crayon.fieldapp.ui.screen.videoDialog.VideoDialog
import com.crayon.fieldapp.utils.FileManager
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.Utils
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showConfirmDialog
import com.crayon.fieldapp.utils.showMessageDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class InputBillFragment(val onNextClick: (CreateCustomerBillResponse) -> Unit = {}) :
    BaseFragment<FragmentInputBillBinding, InputBillViewModel>() {

    override val layoutId: Int = R.layout.fragment_input_bill
    override val viewModel: InputBillViewModel by viewModel()
    private var taskId: String? = null
    private var _customerId: String? = null
    private lateinit var updateImageAdapter: UploadMediaAdapter
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskId = requireArguments().getString("taskId").toString()
        updateImageAdapter =
            UploadMediaAdapter(
                items = arrayListOf(),
                context = requireContext(),
                onItemClick = { data: MediaData, position: Int ->
                    if (data.type == ImageAdapter.MEDIA_IMAGE) {
                        val imageDialog = ImageDialog(
                            title = data.note ?: "",
                            imageUrl = data.uri
                        )
                        imageDialog.show(childFragmentManager, imageDialog.tag)
                    } else {
                        val videoDialog = VideoDialog(
                            title = data.note ?: "",
                            imageUrl = data.uri
                        )
                        videoDialog.show(childFragmentManager, videoDialog.tag)
                    }
                },
                removeClickListener = { data: MediaData, position: Int ->
                    context.showConfirmDialog(
                        title = "Bạn có muốn xoá ảnh này không?",
                        textPositive = "Có",
                        textNegative = "Không",
                        positiveListener = {
                            updateImageAdapter.deleteImage(position)
                        }
                    )
                }
            )

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    val selectedImageUri = data?.data
                    val path = FileManager.getPath(requireContext(), selectedImageUri)
                    showImage(path)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("url")
            ?.observe(viewLifecycleOwner, Observer { result ->
                showImage(result)
            })

        binding.btnNext.setSingleClick {
            Utils.hideKeyboard(requireActivity())
            if (updateImageAdapter.itemCount > 3) {
                context?.showMessageDialog("Bạn chỉ được chụp tối đa 3 tấm")
                return@setSingleClick
            } else {
                if (updateImageAdapter.itemCount != 0) {
                    val bill = binding.edtBill.text.toString().trim()
                    if (bill.isBlank()) {
                        context?.showMessageDialog("Vui lòng nhập mã hóa đơn")
                        return@setSingleClick
                    } else {
                        viewModel.createCustomerBill(
                            taskId = taskId.toString(),
                            customerId = _customerId.toString(),
                            codeBill = bill.toString(),
                            listUri = updateImageAdapter.getData()
                        )
                    }

                } else {
                    context?.showMessageDialog("Bạn chưa có chụp ảnh")
                }
            }
        }

        binding.btnCamera.setSingleClick {
            openCamera()
        }

        binding.btnGallery.setSingleClick {
            openGallery()
        }

        viewModel.createCustomerBill.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            onNextClick(it)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })

        binding.rvImages.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = updateImageAdapter
        }
    }

    private fun openCamera() {
        if (updateImageAdapter.itemCount >= 3) {
            context?.showMessageDialog("Bạn chỉ được chụp tối đa 3 tấm")
        } else {
            val bundle = bundleOf("isTakeImage" to true)
            findNavController().navigate(R.id.action_global_CameraFragment, bundle)
        }
    }

    private fun openGallery() {
        if (updateImageAdapter.itemCount >= 3) {
            context?.showMessageDialog("Bạn chỉ được chụp tối đa 3 tấm")
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val chooserIntent = Intent.createChooser(intent, "Select Picture")
            galleryLauncher.launch(chooserIntent)
        }
    }

    private fun showImage(url: String) {
        var type = 0
        if (url.contains("mp4")) {
            type = MediaAdapter.MEDIA_VIDEO
        } else {
            type = MediaAdapter.MEDIA_IMAGE
        }
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                updateImageAdapter.addImages(
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
            val isHasTag = false
            val size = 50
            val file = viewModel.createFile(requireContext(), url, TaskResponse(), size, isHasTag)
            file?.let { myFile ->
                withContext(Dispatchers.Main) {
                    updateImageAdapter.updateImage(
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

    fun setCustomerId(mCustomerId: String) {
        this._customerId = mCustomerId
    }

    companion object {
        const val CODE_REQUEST_GALLERY = 1
    }
}