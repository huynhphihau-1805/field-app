package com.crayon.fieldapp.ui.screen.notification

//import androidx.recyclerview.widget.MergeAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentNotificationBinding
import com.crayon.fieldapp.fcm.MyFirebaseMessagingService
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.MemberRequestAdapter
import com.crayon.fieldapp.ui.base.adapter.NotificationAdapter
import com.crayon.fieldapp.ui.base.adapter.SwitchShiftRequestAdapter
import com.crayon.fieldapp.utils.Status
import org.koin.androidx.viewmodel.ext.android.viewModel

class NotificationFragment :
    BaseFragment<FragmentNotificationBinding, NotificationViewModel>() {

    override val layoutId: Int = R.layout.fragment_notification

    override val viewModel: NotificationViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        reloadData()

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            reloadData()
        }

        val memberRequestAdapter = MemberRequestAdapter(
            acceptClickListener = {
                viewModel.acceptMemberRequest(it.id.toString())
            },
            rejectClickListener = {
                viewModel.rejectMemberRequest(it.id.toString())
            }
        )


        val notificationAdapter = NotificationAdapter(
            itemClickListener = {
            }
        )

        val switchShiftAdapter = SwitchShiftRequestAdapter(
            acceptClickListener = {
                val ids = arrayListOf(it.id)
                viewModel.acceptSwitchShiftRequest(ids as ArrayList<String>)
            },
            rejectClickListener = {
                val ids = arrayListOf(it.id)
                viewModel.rejectSwitchShiftRequest(ids as ArrayList<String>)
            }
        )

        val allAdapter =
            ConcatAdapter(memberRequestAdapter, switchShiftAdapter, notificationAdapter)

        binding.rvNotification.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = allAdapter
        }


        viewModel.memberRequests.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            memberRequestAdapter.submitList(it)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.notifications.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            notificationAdapter.submitList(it)
                            binding.rvNotification.scrollToPosition(0)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.switchShiftRequests.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            switchShiftAdapter.submitList(it)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.isAcceptMemberRequestSuccess.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        reloadData()
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.isAcceptSwitchShiftRequestSuccess.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        reloadData()
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.isRejectSwitchShiftRequestSuccess.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        reloadData()
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.isRejectMemberRequestSuccess.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        reloadData()
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun reloadData() {
        viewModel.apply {
            getMemberRequest("Pending")
            getSwitchShift("Pending")
            getNotifications()
        }
    }


    private val mPushFilter = IntentFilter(MyFirebaseMessagingService.PUSH_ANNOUNCE)

    override fun onResume() {
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mPushReceiver, mPushFilter)
        super.onResume()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mPushReceiver)
        super.onPause()
    }


    private val mPushReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("AAA", "onReceive")
            reloadData()
        }
    }
}