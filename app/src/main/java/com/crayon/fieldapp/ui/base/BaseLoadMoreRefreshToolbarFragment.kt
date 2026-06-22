package com.crayon.fieldapp.ui.base

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import com.crayon.fieldapp.R

abstract class BaseLoadMoreRefreshToolbarFragment<
        VB : ViewDataBinding,
        VM : BaseLoadMoreRefreshToolbarViewModel<Item>,
        Item
        > : BaseFragment<VB, VM>() {

    override val layoutId: Int = R.layout.fragment_loadmore_refresh_toolbar

    override fun handleLoading(isLoading: Boolean) {
        binding.root.findViewById<View>(R.id.progressBar)?.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.findViewById<View>(R.id.imbIcBack)?.setOnClickListener {
            navigateUp()
        }
    }
}
