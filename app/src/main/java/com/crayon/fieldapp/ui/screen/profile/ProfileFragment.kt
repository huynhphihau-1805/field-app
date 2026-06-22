package com.crayon.fieldapp.ui.screen.profile

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.BuildConfig
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentProfileBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.dialog.LoginQrCodeDialog
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.loadImage
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showConfirmDialog
import com.crayon.fieldapp.utils.showMessageDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileViewModel>() {

    override val layoutId: Int = R.layout.fragment_profile

    override val viewModel: ProfileViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getUserInfo()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.apply {
            isLogoutSuccess.observe(viewLifecycleOwner, Observer {
                findNavController().navigate(R.id.action_main_to_login)
            })

            loginByQrCode.observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let {
                    when (it.status) {
                        Status.LOADING -> {
                        }

                        Status.SUCCESS -> {
                            it.data?.let {
                                context?.showMessageDialog(it.message)
                            }
                        }

                        Status.ERROR -> {
                        }
                    }
                }
            })

            user.observe(viewLifecycleOwner, Observer { userInfo ->
                binding.tvUsername.text = userInfo.lastName + " " + userInfo.firstName

                binding.imgAvatar.loadImage(
                    imageUrl = userInfo.avatarUrl,
                    centerCrop = true,
                    circleCrop = true,
                    signatureKey = userInfo.updatedAt,
                    fitCenter = true
                )

                binding.imgIcAvatar.loadImage(
                    imageUrl = userInfo.avatarUrl,
                    centerCrop = true,
                    circleCrop = true,
                    signatureKey = userInfo.updatedAt,
                    fitCenter = true
                )
            })

        }

        binding.rlAvatar.setSingleClick {
            findNavController().navigate(R.id.action_profile_to_avatar)
        }

        binding.rlContactInfo.setSingleClick {
            findNavController().navigate(R.id.action_profile_to_infoFragment)
        }

        binding.rlBank.setSingleClick {
            findNavController().navigate(R.id.action_profile_to_bankFragment)
        }

        binding.rlApplication.setSingleClick {
            findNavController().navigate(R.id.action_profile_to_applicationFragment)
        }

        binding.rlAboutTerms.setSingleClick {
            findNavController().navigate(R.id.action_profile_to_termFragment)
        }

        binding.rlAboutPrivacy.setSingleClick {
            findNavController().navigate(R.id.action_profile_to_privacyFragment)
        }

        binding.rlContact.setSingleClick {
            findNavController().navigate(R.id.action_profile_to_contactFragment)
        }

        binding.rlLoginQrcod.setSingleClick {
            val qrCodeDialog = LoginQrCodeDialog({
                viewModel.loginByQrCode(it.replace("\"", ""))
            })
            qrCodeDialog.show(childFragmentManager, qrCodeDialog.tag)
        }

        binding.tvVersion.text = BuildConfig.VERSION_NAME

        binding.tvLogout.setOnClickListener {
            requireContext().showConfirmDialog(
                title = "Bạn có muốn đăng xuất không?",
                textPositive = "Có",
                textNegative = "Không",
                positiveListener = {
                    viewModel.logout()
                }
            )

        }

    }
}