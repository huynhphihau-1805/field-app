package com.crayon.fieldapp.ui.screen.monitor.store.listStore

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentListStoreBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.StoreAdapter
import com.crayon.fieldapp.utils.Status
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListStoreFragment : BaseFragment<FragmentListStoreBinding, ListStoreViewModel>() {

    override val layoutId: Int = R.layout.fragment_list_store

    override val viewModel: ListStoreViewModel by viewModel()

    private lateinit var agencyId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = StoreAdapter(
            itemClickListener = { toStoreDetail(it.id.toString()) }
        )

        binding.rvStore.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.apply {
            stores.observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            adapter.submitList(it)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            })
            getStores(agencyId)
        }

    }

    private fun toStoreDetail(storeId: String) {
        val bundel = bundleOf("storeId" to storeId, "agencyId" to agencyId)
        findNavController().navigate(R.id.action_manageStoreFragment_to_detailStoreFragment, bundel)
    }
}