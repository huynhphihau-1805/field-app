package com.crayon.fieldapp.ui.screen.job.listJob.adapter

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

class JobLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<JobLoadStateViewHolder>() {
    override fun onBindViewHolder(holder: JobLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): JobLoadStateViewHolder {
        return JobLoadStateViewHolder.create(parent, retry)
    }
}
