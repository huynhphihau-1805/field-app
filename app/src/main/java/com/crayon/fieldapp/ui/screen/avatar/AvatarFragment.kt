package com.crayon.fieldapp.ui.screen.avatar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentAvatarBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.dialog.getPhoto.GetPhotoDialogFragment
import com.crayon.fieldapp.utils.FileManager
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.loadImage
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showMessageDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class AvatarFragment : BaseFragment<FragmentAvatarBinding, AvatarViewModel>(),
    GetPhotoDialogFragment.GetPhotoDialogListener {

    override val layoutId: Int = R.layout.fragment_avatar

    override val viewModel: AvatarViewModel by viewModel()

    private var tyeImage: String = ""
    private var avatarRef: Uri? = null
    private var bodyRef: Uri? = null
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    val selectedImageUri = data?.data
                    val path = FileManager.getPath(requireContext(), selectedImageUri)
                    val file = viewModel.createFile(path, 100)
                    when (tyeImage) {
                        "avatar" -> {
                            binding.imgIcAvatar.loadImage(
                                imageUrl = file!!.absolutePath,
                                centerCrop = true,
                                circleCrop = true,
                                fitCenter = true
                            )
                            avatarRef = Uri.fromFile(file)
                            avatarRef?.let { avatar ->
                                viewModel.updateAvatar(avatar)
                            }
                        }

                        "body" -> {
                            binding.imgIcBody.loadImage(
                                imageUrl = file!!.absolutePath,
                                centerCrop = true,
                                fitCenter = true
                            )
                            bodyRef = Uri.fromFile(file)
                            bodyRef?.let { body ->
                                viewModel.updateFullBody(body)
                            }
                        }
                    }
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.rlAvatar.setSingleClick {
            tyeImage = "avatar"
            openCamera()
        }

        binding.rlBody.setSingleClick {
            tyeImage = "body"
            openCamera()
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("url")
            ?.observe(viewLifecycleOwner, Observer { result ->
                val file =
                    viewModel.createImageFileToUpload(requireContext(), result, tyeImage)
                when (tyeImage) {
                    "avatar" -> {
                        binding.imgIcAvatar.loadImage(
                            imageUrl = Uri.fromFile(file).path,
                            centerCrop = true,
                            circleCrop = true,
                            fitCenter = true
                        )

                        avatarRef = Uri.fromFile(file)
                        avatarRef?.let { avatar ->
                            viewModel.updateAvatar(avatar)
                        }
                    }

                    "body" -> {
                        binding.imgIcBody.loadImage(
                            imageUrl = Uri.fromFile(file).path,
                            centerCrop = true,
                            fitCenter = true
                        )
                        bodyRef = Uri.fromFile(file)
                        bodyRef?.let { body ->
                            viewModel.updateFullBody(body)
                        }
                    }
                }
            })


        viewModel.apply {
            getUserInfo()
            avatar.observe(viewLifecycleOwner, Observer {
                Log.d("AAA-avatar", it.data.toString())
                when (it.status) {
                    Status.LOADING -> {
                        showLoading()
                    }

                    Status.SUCCESS -> {
                        hideLoading()
                        if (tyeImage.equals("avatar")) {
                            context?.showMessageDialog("Cập nhật hình đại diện thành công")
                        } else {
                            context?.showMessageDialog("Cập nhật hình toàn thân thành công")
                        }

                    }

                    Status.ERROR -> {
                        hideLoading()
                    }
                }
            })
            user.observe(viewLifecycleOwner, Observer { userInfo ->
                binding.imgIcAvatar.loadImage(
                    imageUrl = userInfo.avatarUrl,
                    centerCrop = true,
                    circleCrop = true,
                    signatureKey = userInfo.updatedAt,
                    fitCenter = true
                )

                binding.tvTitle.text = userInfo.lastName + " " + userInfo.firstName

                binding.imgIcBody.loadImage(
                    imageUrl = userInfo.profile!!.fullBodyImageUrl,
                    centerCrop = true,
                    signatureKey = userInfo.updatedAt,
                    fitCenter = true
                )
            })
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        galleryLauncher.launch(intent)
    }

    private fun openCamera() {
        val bundle = bundleOf("isTakeImage" to true)
        findNavController().navigate(R.id.action_global_CameraFragment, bundle)
    }

    companion object {
        const val CODE_REQUEST_GALLERY = 1
    }

    override fun selectFromGallery() {
        openGallery()
    }

    override fun selectFromCamera() {
        openCamera()
    }


}