package com.crayon.fieldapp.ui.screen.selectProject

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.PicProjectResponse
import com.crayon.fieldapp.databinding.FragmentSelectProjectBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import studio.phillip.yolo.presentation.add.selectProject.adapter.SelectProjectRVAdapter

class SelectProjectFragment : BaseFragment<FragmentSelectProjectBinding, SelectProjectViewModel>() {

    override val layoutId: Int = R.layout.fragment_select_project

    override val viewModel: SelectProjectViewModel by viewModel()

    private var adapterMembers: SelectProjectRVAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.btnChoose.setSingleClick {
            binding.pbLoading.visibility = View.VISIBLE
            val ids = adapterMembers?.getSelectProject()
            val strIds = Gson().toJson(ids)
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                "ids",
                strIds
            )
            findNavController().navigateUp()
        }

        binding.cbSelectAll?.setOnCheckedChangeListener { compoundButton, isChecked ->
            adapterMembers?.selectAllItems(isChecked)
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapterMembers = SelectProjectRVAdapter(
            arrayListOf(),
            context = requireContext(),
            itemClickListener = {

            }
        )

        binding.rvProject.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapterMembers
        }

        viewModel.apply {
            getProject()

            projects.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            adapterMembers?.addUser(it as ArrayList<PicProjectResponse>)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            })
        }

    }
}