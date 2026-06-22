package com.crayon.fieldapp.ui.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.BR
import com.crayon.fieldapp.R
import com.crayon.fieldapp.utils.showDialog
import com.crayon.fieldapp.utils.showLoadingDialog

abstract class BaseDialogFragment<ViewBinding : ViewDataBinding, ViewModel : BaseViewModel> :
    DialogFragment() {

    protected lateinit var binding: ViewBinding
    protected abstract val viewModel: ViewModel

    @get:LayoutRes
    protected abstract val layoutId: Int

    private var loadingDialog: AlertDialog? = null
    private var messageDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding.apply {
            setVariable(BR.viewModel, viewModel)
            lifecycleOwner = viewLifecycleOwner
            executePendingBindings()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.apply {
            isLoading.observe(viewLifecycleOwner) { handleLoading(it == true) }
            errorMessage.observe(viewLifecycleOwner) { handleErrorMessage(it) }
            noInternetConnectionEvent.observe(viewLifecycleOwner) {
                handleErrorMessage(getString(R.string.no_internet_connection))
            }
            connectTimeoutEvent.observe(viewLifecycleOwner) {
                handleErrorMessage(getString(R.string.connect_timeout))
            }
        }
    }

    open fun handleLoading(isLoading: Boolean) {
        if (isLoading) showLoadingDialog() else dismissLoadingDialog()
    }

    private fun showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = context?.showLoadingDialog()
        } else {
            loadingDialog?.show()
        }
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.takeIf { it.isShowing }?.dismiss()
    }

    private fun handleErrorMessage(message: String?) {
        message?.takeUnless { it.isBlank() }?.let {
            dismissLoadingDialog()

            messageDialog?.takeIf { it.isShowing }?.dismiss()

            messageDialog = context?.showDialog(
                message = it,
                textPositive = getString(R.string.ok)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingDialog?.dismiss()
        messageDialog?.dismiss()
    }

    // Helper methods for fragment transactions
    fun navigateUp() {
        findNavController().navigateUp()
    }
}
