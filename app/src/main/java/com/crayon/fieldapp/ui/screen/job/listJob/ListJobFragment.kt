package com.crayon.fieldapp.ui.screen.job.listJob

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentListMyJobBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.job.listJob.adapter.JobAdapter
import com.crayon.fieldapp.ui.screen.job.listJob.adapter.JobLoadStateAdapter
import com.crayon.fieldapp.utils.setSingleClick
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListJobFragment : BaseFragment<FragmentListMyJobBinding, ListJobViewModel>() {

    override val layoutId: Int = R.layout.fragment_list_my_job

    override val viewModel: ListJobViewModel by viewModel()
    private lateinit var status: String
    private var searchJob: Job? = null
    val adapter = JobAdapter() {
        toJobDetail(it)
    }

    private fun search(status: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            viewModel.getJobByStatus(status).collectLatest {
                adapter.submitData(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        status = requireArguments().getString("status").toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initSearch()
        search(status)

        binding.retryButton.setSingleClick {
            adapter.retry()
        }

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            adapter.refresh()
        }

        binding.rvJob.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
    }

    private fun initSearch() {
        // Scroll to top when the list is refreshed from network.
        lifecycleScope.launch {
            adapter.loadStateFlow
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .collect { binding.rvJob.scrollToPosition(0) }
        }
    }

    private fun initAdapter() {
        binding.rvJob.adapter = adapter.withLoadStateHeaderAndFooter(
            header = JobLoadStateAdapter { adapter.retry() },
            footer = JobLoadStateAdapter { adapter.retry() }
        )
        adapter.addLoadStateListener { loadState ->
            binding.rvJob.isVisible = loadState.source.refresh is LoadState.NotLoading
            binding.pbLoading.isVisible = loadState.source.refresh is LoadState.Loading
            binding.retryButton.isVisible = loadState.source.refresh is LoadState.Error

            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
            errorState?.let {
                Toast.makeText(
                    requireContext(),
                    "\uD83D\uDE28 Wooops ${it.error}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    private fun toJobDetail(id: String) {
        val bundel = bundleOf("jobId" to id)
        findNavController().navigate(R.id.action_listjob_to_jobDetail, bundel)
    }
}