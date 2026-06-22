package com.crayon.fieldapp.ui.screen.contact

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentContactBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.utils.setSingleClick
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactFragment : BaseFragment<FragmentContactBinding, ContactViewModel>() {

    override val layoutId: Int = R.layout.fragment_contact

    override val viewModel: ContactViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }
    }
}