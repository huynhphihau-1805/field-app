package com.crayon.fieldapp.ui.screen.detailTask.reportSales.detailOrder

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.OrderResponse
import com.crayon.fieldapp.databinding.FragmentDetailOrderBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.ChangeGiftViewModel
import com.crayon.fieldapp.ui.screen.detailTask.reportSales.detailOrder.adapter.DetailOrderRVAdapter
import com.crayon.fieldapp.utils.setSingleClick
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat

class DetailOrderFragment : BaseFragment<FragmentDetailOrderBinding, ChangeGiftViewModel>() {

    override val layoutId: Int = R.layout.fragment_detail_order
    override val viewModel: ChangeGiftViewModel by viewModel()
    private lateinit var mOrderAdapter: DetailOrderRVAdapter
    private var _orderResponse: OrderResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val order = arguments?.getString("order")
        order?.let {
            _orderResponse = Gson().fromJson(it, OrderResponse::class.java)
        }

        mOrderAdapter =
            DetailOrderRVAdapter(
                arrayListOf(), requireContext(), {
                    // Item
                })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        _orderResponse?.let {
            val totalPrice = it.products.sumBy { it.price * it.quantity }
            val format = DecimalFormat("#,###")
            format.maximumFractionDigits = 0
            binding.txtTotal.text = format.format(totalPrice) + "vnd"
            mOrderAdapter.addAll(it.products)

        }

        binding.rvProduct.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = mOrderAdapter
        }

    }
}