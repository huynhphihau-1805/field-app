package com.crayon.fieldapp.ui.screen.detailUser

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentDetailUserBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.utils.setSingleClick
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailUserFragment : BaseFragment<FragmentDetailUserBinding, DetailUserViewModel>() {

    override val layoutId: Int = R.layout.fragment_detail_user

    override val viewModel: DetailUserViewModel by viewModel()

    private var agencyId: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = arguments?.getString("agencyId").toString()
        userId = arguments?.getString("userId").toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        viewModel.apply {
            fetchDetailUser(agencyId.toString(), userId.toString())
        }
    }
}