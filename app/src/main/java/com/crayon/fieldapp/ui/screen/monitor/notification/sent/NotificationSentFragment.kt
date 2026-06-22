package com.crayon.fieldapp.ui.screen.monitor.notification.sent

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.model.Notification
import com.crayon.fieldapp.data.model.Store
import com.crayon.fieldapp.databinding.FragmentLoadmoreRefreshBinding
import com.crayon.fieldapp.ui.base.BaseLoadMoreRefreshFragment
import com.crayon.fieldapp.ui.base.adapter.NotificationAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class NotificationSentFragment :
    BaseLoadMoreRefreshFragment<FragmentLoadmoreRefreshBinding, NotificationSentViewModel, Store>() {

    lateinit var agencyId: String

    override val viewModel: NotificationSentViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agencyId = requireArguments().get("agencyId").toString()
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = NotificationAdapter(
            itemClickListener = { toNotificationDetail(it.id.toString()) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.apply {
            listItem.observe(viewLifecycleOwner, Observer {
                val list = ArrayList<Notification>()
//                list.add(
//                    Notification(
//                        id = "1",
//                        title = "Title 1",
//                        content = "Content 1",
//                        createDate = "2020-08-19T08:59:50.408Z",
//                        thumbnail = "https://mtsstoragebucket.sgp1.digitaloceanspaces.com/partner/84919762648/avatar.jpeg"
//                    )
//                )
//                list.add(
//                    Notification(
//                        id = "2",
//                        title = "Title 1",
//                        content = "Content 1",
//                        createDate = "2020-08-19T08:59:50.408Z",
//                        thumbnail = "https://mtsstoragebucket.sgp1.digitaloceanspaces.com/partner/84919762648/avatar.jpeg"
//                    )
//                )
//                list.add(
//                    Notification(
//                        id = "3",
//                        title = "Title 1",
//                        content = "Content 1",
//                        createDate = "2020-08-19T08:59:50.408Z",
//                        thumbnail = "https://mtsstoragebucket.sgp1.digitaloceanspaces.com/partner/84919762648/avatar.jpeg"
//                    )
//                )
//                list.add(
//                    Notification(
//                        id = "4",
//                        title = "Title 1",
//                        content = "Content 1",
//                        createDate = "2020-08-19T08:59:50.408Z",
//                        thumbnail = "https://mtsstoragebucket.sgp1.digitaloceanspaces.com/partner/84919762648/avatar.jpeg"
//                    )
//                )
//                adapter.submitList(list)
            })
            firstLoad(agencyId)
        }
    }

    private fun toNotificationDetail(id: String) {
        val bundel = bundleOf("storeId" to id, "agencyId" to agencyId)
        findNavController().navigate(R.id.to_detail_notification, bundel)
    }
}