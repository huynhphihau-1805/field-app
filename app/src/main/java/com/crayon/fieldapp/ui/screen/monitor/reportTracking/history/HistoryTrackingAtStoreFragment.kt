package com.crayon.fieldapp.ui.screen.monitor.reportTracking.history

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentHistoryTrackingBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.monitor.reportTracking.history.adapter.HistoryTrackingRVAdapter
import com.crayon.fieldapp.utils.setSingleClick
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryTrackingAtStoreFragment() :
    BaseFragment<FragmentHistoryTrackingBinding, HistoryTrackingAtStoreViewModel>() {
    override val viewModel: HistoryTrackingAtStoreViewModel by viewModel()
    override val layoutId: Int
        get() = R.layout.fragment_history_tracking


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.imbIcBack.setSingleClick {
            findNavController().popBackStack()
        }

        val mTrackingAdapter =
            HistoryTrackingRVAdapter(
                arrayListOf(
                    "10:15",
                    "10:00",
                    "09:45",
                    "09:30",
                    "09:15",
                    "09:00",
                    "08:45",
                    "08:30"
                ), requireContext(), {
                    // Item
                    findNavController().navigate(
                        R.id.action_history_tracking_to_detail_tracking
                    )
                })

        binding.rvTracking.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = mTrackingAdapter
        }

    }

}