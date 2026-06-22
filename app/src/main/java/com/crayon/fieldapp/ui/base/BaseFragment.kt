package com.crayon.fieldapp.ui.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.local.pref.PrefHelper
import com.crayon.fieldapp.databinding.DialogEditQuantityBinding
import com.crayon.fieldapp.utils.DialogHandler
import com.crayon.fieldapp.utils.Utils
import com.crayon.fieldapp.utils.showLoadingDialog
import org.koin.android.ext.android.inject


abstract class BaseFragment<VB : ViewDataBinding, VM : BaseViewModel> : Fragment() {
    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected abstract val viewModel: VM
    val appPrefs: PrefHelper by inject()

    @get:LayoutRes
    protected abstract val layoutId: Int

    private var loadingDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.apply {
            isLoading.observe(viewLifecycleOwner) { handleLoading(it == true) }
            errorMessage.observe(viewLifecycleOwner) { handleErrorMessage(it) }
            noInternetConnectionEvent.observe(viewLifecycleOwner) {
                handleErrorMessage(getString(R.string.no_internet_connection))
            }
            tokenExpiredEvent.observe(viewLifecycleOwner) {
                appPrefs.clear()
                navigateToLogin()
            }
            connectTimeoutEvent.observe(viewLifecycleOwner) {
                handleErrorMessage(getString(R.string.connect_timeout))
            }
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_global_loginFragment)
    }

    open fun handleLoading(isLoading: Boolean) {
        if (isLoading) showLoadingDialog() else dismissLoadingDialog()
    }

    fun showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = context?.showLoadingDialog()
        }
        loadingDialog?.show()
    }

    fun dismissLoadingDialog() {
        if (loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
        }
    }

    fun handleErrorMessage(message: String?) {
        dismissLoadingDialog()
        DialogHandler.showMessageDialog(requireContext(), message.orEmpty())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog?.dismiss()
        _binding = null
        DialogHandler.dismissMessageDialog()
    }

    override fun onStop() {
        super.onStop()
        Utils.hideKeyboard(requireActivity())
    }

    fun navigateUp() {
        findNavController().navigateUp()
    }
}
