package com.crayon.fieldapp.ui.screen.detailGroupStore

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentDetailGroupStoreBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.StoreAdapter
import com.crayon.fieldapp.utils.setSingleClick
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailGroupFragment :
    BaseFragment<FragmentDetailGroupStoreBinding, DetailGroupStoreViewModel>() {

    override val layoutId: Int = R.layout.fragment_detail_group_store
    override val viewModel: DetailGroupStoreViewModel by viewModel()

    lateinit var agencyId: String
    lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
        groupId = requireArguments().get("groupId").toString()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = StoreAdapter(
            itemClickListener = { toStoreDetail(it.id.toString()) }
        )

        binding.rvStore.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.apply {
            getDetailGroup(agencyId, groupId)

            groups.observe(viewLifecycleOwner, Observer {
                binding.tvTitle.text = it.name
                adapter.submitList(it.stores)
            })
        }

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }
    }

    private fun toStoreDetail(id: String) {
        val bundel = bundleOf("storeId" to id, "agencyId" to agencyId)
        findNavController().navigate(R.id.action_detailGroupFragment_to_detailStoreFragment, bundel)
    }
}