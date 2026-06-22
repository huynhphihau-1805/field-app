package com.crayon.fieldapp.ui.screen.detailProject.createTask

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.request.CreateTaskForm
import com.crayon.fieldapp.data.remote.response.TaskType
import com.crayon.fieldapp.databinding.FragmentCreateTaskBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailProject.createTask.adapter.TaskTypeSPAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateTaskFragment : BaseFragment<FragmentCreateTaskBinding, CreateTaskViewModel>() {

    override val layoutId: Int = R.layout.fragment_create_task

    override val viewModel: CreateTaskViewModel by viewModel()

    private lateinit var agencyId: String
    private lateinit var projectId: String
    private lateinit var jobId: String
    private lateinit var storeId: String
    private lateinit var picId: String
    private lateinit var taskTypeAdapter: TaskTypeSPAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
        projectId = requireArguments().get("projectId").toString()
        jobId = requireArguments().get("jobId").toString()
        storeId = requireArguments().get("storeId").toString()
        picId = requireArguments().get("picId").toString()
        taskTypeAdapter = TaskTypeSPAdapter(TaskType.values(), requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.spType.adapter = taskTypeAdapter

        binding.imbIcFilter.setSingleClick {
            val form = CreateTaskForm(
                agency = agencyId,
                project = projectId,
                store = storeId,
                job = jobId,
                pic = picId,
                name = "Name",
                description = "description",
                type = taskTypeAdapter.getItem(binding.spType.selectedItemPosition).value.toString()
            )
            form.validate().also { result ->
                if (result.first) {
                    viewModel.createTask(form)
                } else {
                    handleErrorMessage(result.second)
                }
            }
        }

        viewModel.apply {
            createTask.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                when (it.status) {
                    Status.LOADING -> {
                        showLoading()
                    }

                    Status.SUCCESS -> {
                        hideLoading()
                        requireContext().showDialog(
                            message = "Đã tạo nhiệm vụ",
                            title = "Thông báo",
                            positiveListener = {
                                findNavController().navigateUp()
                            }
                        )
                    }

                    Status.ERROR -> {
                        hideLoading()
                    }
                }
            })
        }

    }
}