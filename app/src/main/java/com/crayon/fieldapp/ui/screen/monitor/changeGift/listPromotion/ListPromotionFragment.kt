package com.crayon.fieldapp.ui.screen.monitor.changeGift.listPromotion

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentListPromotionBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.monitor.changeGift.listPromotion.adapter.PromotionRVAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListPromotionFragment() :
    BaseFragment<FragmentListPromotionBinding, ListPromotionViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_list_promotion

    private var mAdapter: PromotionRVAdapter? = null

    override val viewModel: ListPromotionViewModel by viewModel()
    var agencyId: String? = null
    var projectId: String? = null
    var startDate: String? = null
    var endDate: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
        projectId = requireArguments().get("projectId").toString()
        startDate = requireArguments().get("startDate").toString()
        endDate = requireArguments().get("endDate").toString()

        mAdapter = PromotionRVAdapter(
            arrayListOf(),
            requireContext(),
            itemClickListener = {
            }
        )

        viewModel.getProjectSummary(
            agencyId = agencyId.toString(),
            projectId = projectId.toString(),
            startDate = startDate.toString(),
            endDate = endDate.toString()
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvMembers.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = mAdapter
        }

        binding.imbIcBack.setSingleClick {
            findNavController().popBackStack()
        }

        viewModel.apply {
            summary.observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let {
                    when (it.status) {
                        Status.LOADING -> {
                            binding.pbLoading.visibility = View.VISIBLE
                        }

                        Status.SUCCESS -> {
                            binding.pbLoading.visibility = View.GONE
                            binding.rvMembers.visibility = View.VISIBLE
                            it.data?.let { mListTasks ->
                                mAdapter?.addAll(mListTasks)
                            }
                        }

                        Status.ERROR -> {
                            binding.pbLoading.visibility = View.GONE
                        }
                    }
                }
            })
        }
    }
}