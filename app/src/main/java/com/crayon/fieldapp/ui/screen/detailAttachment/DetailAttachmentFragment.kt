package com.crayon.fieldapp.ui.screen.detailAttachment

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.databinding.FragmentDetailAttachmentBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.adapter.BaseVPAdapter
import com.crayon.fieldapp.ui.screen.detailAttachment.image.ListImageFragment
import com.crayon.fieldapp.ui.screen.detailAttachment.product.ListProductFragment
import com.crayon.fieldapp.utils.setSingleClick
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailAttachmentFragment :
    BaseFragment<FragmentDetailAttachmentBinding, DetailAttachmentViewModel>() {

    override val layoutId: Int = R.layout.fragment_detail_attachment
    override val viewModel: DetailAttachmentViewModel by viewModel()

    private var task: TaskResponse? = null
    private lateinit var mAdapter: BaseVPAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val taskString = requireArguments().get("task").toString()
        task = Gson().fromJson(taskString, TaskResponse::class.java)
    }

    lateinit var agencyId: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tab_attachment)
        viewPager = view.findViewById(R.id.vp_attachment)

        setupViewPager()
        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }
    }


    private fun setupViewPager() {
        mAdapter = BaseVPAdapter(childFragmentManager)
        val listImageFragment = ListImageFragment()
        val listVideoFragment = ListImageFragment()
        val listProductFragment = ListProductFragment()
        val taskString = Gson().toJson(task).toString()
        val bundleImage = bundleOf("task" to taskString, "type" to "image")
        val bundleVideo = bundleOf("task" to taskString, "type" to "video")
        val bundleProudct = bundleOf("task" to task?.id.toString())

        listImageFragment.arguments = bundleImage
        listVideoFragment.arguments = bundleVideo
        listProductFragment.arguments = bundleProudct
        mAdapter.addFragment(listImageFragment, "Ảnh")
        mAdapter.addFragment(listVideoFragment, "Video")
        mAdapter.addFragment(listProductFragment, "Sản phẩm")

        viewPager.apply {
            offscreenPageLimit = 3
            adapter = mAdapter
        }
        tabLayout.setupWithViewPager(viewPager)
    }
}