package com.crayon.fieldapp.ui.screen.detailTask.changeGift.step4

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.GiftResponse
import com.crayon.fieldapp.data.remote.response.ProductResponse
import com.crayon.fieldapp.data.remote.response.PromotionResponse
import com.crayon.fieldapp.databinding.FragmentSelectPromotionBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.selectProduct.SelectProductBottomSheetFragment
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.step4.adapter.SelectPromotionRVAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.Utils
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showMessageDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectPromotionFragment(val onNextClick: () -> Unit = {}) :
    BaseFragment<FragmentSelectPromotionBinding, SelectPromotionViewModel>() {

    override val layoutId: Int = R.layout.fragment_select_promotion
    override val viewModel: SelectPromotionViewModel by viewModel()
    private var taskId: String? = null
    private var _projectId: String? = null
    private var _products: ArrayList<ProductResponse> = arrayListOf()
    private var _billId: String? = null

    private var mDetailRVAdapter: SelectPromotionRVAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskId = requireArguments().getString("taskId").toString()
        _projectId = requireArguments().getString("projectId")

        _projectId?.let {
            viewModel.getListProductAndPromotions(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDetailRVAdapter = SelectPromotionRVAdapter(
            promotion = arrayListOf(),
            gifts = arrayListOf(),
            context = requireContext(),
            onShowSelectProduct = { mPromotion ->
                val items = ArrayList<ProductResponse>()
                _products.forEach {
                    items.add(it.copy())
                }
                val dialog = SelectProductBottomSheetFragment(
                    items,
                    onSelectProductListener = { mProducts ->
                        mDetailRVAdapter?.addAllProduct(mPromotion, mProducts)
                    }, onUpdatePriceListener = { product, price ->
                        _products.indexOfFirst { _item ->
                            _item.id.toString().equals(product.id.toString())
                        }?.let {
                            if (it != -1) {
                                _products.get(it).price = price
                                _products.get(it).isEdit = true
                            }
                        }
                        viewModel.updatePrice(product, price = price.toLong())
                    })
                dialog.show(requireActivity().supportFragmentManager, dialog.tag)
            },
            onItemClick = {

            })

        binding.rvPage.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = mDetailRVAdapter
        }
        binding.btnComplete.setSingleClick {
            Utils.hideKeyboard(requireActivity())
            Log.d("AAAhttp", _billId.toString())
            if (_billId.isNullOrEmpty()) {
                requireContext().showMessageDialog(title = "Không tìm thấy mã hóa đơn")
                return@setSingleClick
            }
            if (taskId.isNullOrEmpty()) {
                requireContext().showMessageDialog(title = "Không tìm thấy mã nhiệm vụ")
                return@setSingleClick
            }
            val request = mDetailRVAdapter?.getSelectPromotions()
            viewModel.addProductToBill(
                taskId = taskId.toString(),
                billId = _billId.toString(),
                request = request!!
            )
        }

        viewModel.productAndPromotion.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            mDetailRVAdapter?.addItems(
                                mPromotion = it.first as ArrayList<PromotionResponse>,
                                mGift = it.third as ArrayList<GiftResponse>
                            )
                            _products.clear()
                            _products.addAll(it.second)
                        }
                    }
                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.addProductToBill.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            requireContext().showMessageDialog(message = it.message.toString())
                        }
                        onNextClick()
                    }
                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })

        binding.rvPage.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = mDetailRVAdapter
        }

    }

    fun setBillId(mBillId: String) {
        this._billId = mBillId
    }
}