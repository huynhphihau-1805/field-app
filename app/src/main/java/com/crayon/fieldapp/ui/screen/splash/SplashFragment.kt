package com.crayon.fieldapp.ui.screen.splash

import android.Manifest
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentSplashBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : BaseFragment<FragmentSplashBinding, SplashViewModel>() {

    override val layoutId: Int = R.layout.fragment_splash

    override val viewModel: SplashViewModel by viewModel()

    private val activityScope = CoroutineScope(Dispatchers.Main)

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val deniedPermissions = permissions.filterValues { !it }.keys

            if (deniedPermissions.isNotEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Quyền bị từ chối: $deniedPermissions",
                    Toast.LENGTH_SHORT
                ).show()
            }

            checkLogin()
        }

    companion object {
        private val multiplePermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onStart() {
        super.onStart()
        activityScope.launch {
            delay(1000)
            requestMultiplePermissions()
        }
    }

    override fun onStop() {
        activityScope.cancel()
        super.onStop()
    }

    private fun requestMultiplePermissions() {
        permissionLauncher.launch(multiplePermissions)
    }

    private fun checkLogin() {
        viewModel.checkLogin()
        viewModel.apply {
            isLogin.observe(viewLifecycleOwner, Observer { isLoggedIn ->
                if (isLoggedIn) {
                    findNavController().navigate(R.id.to_main)
                } else {
                    findNavController().navigate(R.id.to_require)
                }
            })
        }
    }
}