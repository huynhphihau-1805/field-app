package com.crayon.fieldapp.ui.base.adapter

import androidx.recyclerview.widget.DiffUtil
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.databinding.ItemManagementTaskBinding
import com.crayon.fieldapp.ui.base.BaseRecyclerAdapter
import com.crayon.fieldapp.utils.setSingleClick

class ManagementTaskAdapter(
    val itemClickListener: (TaskResponse) -> Unit = {},
    val itemRemoveClickListener: (TaskResponse) -> Unit = {}
) : BaseRecyclerAdapter<TaskResponse, ItemManagementTaskBinding>(object : DiffUtil.ItemCallback<TaskResponse>() {
    override fun areItemsTheSame(oldItem: TaskResponse, newItem: TaskResponse): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TaskResponse, newItem: TaskResponse): Boolean {
        return oldItem == newItem
    }
}) {

    override fun getLayoutRes(viewType: Int): Int {
        return R.layout.item_management_task
    }

    override fun bindFirstTime(binding: ItemManagementTaskBinding) {
        binding.apply {
            root.setSingleClick {
                item?.apply {
                    itemClickListener(this)
                }
            }

            icRemove.setSingleClick {
                item?.apply {
                    itemRemoveClickListener(this)
                }
            }
        }
    }

}