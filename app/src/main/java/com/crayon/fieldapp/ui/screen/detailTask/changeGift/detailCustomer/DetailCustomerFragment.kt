package com.crayon.fieldapp.ui.screen.detailTask.changeGift.detailCustomer

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.CustomerResponse
import com.crayon.fieldapp.data.remote.response.GiftResponse
import com.crayon.fieldapp.data.remote.response.ProductResponse
import com.crayon.fieldapp.data.remote.response.PromotionResponse
import com.crayon.fieldapp.databinding.FragmentDetailCustomerBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.base.dialog.EditQuantityDialog
import com.crayon.fieldapp.ui.screen.detailAttachment.image.ImageAdapter
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaAdapter
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaData
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.adapter.DetailCustomerRVAdapter
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.selectProduct.SelectProductBottomSheetFragment
import com.crayon.fieldapp.ui.screen.imageDialog.ImageDialog
import com.crayon.fieldapp.ui.screen.videoDialog.VideoDialog
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showMessageDialog
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailCustomerFragment :
    BaseFragment<FragmentDetailCustomerBinding, DetailCustomerViewModel>() {

    override val layoutId: Int = R.layout.fragment_detail_customer
    override val viewModel: DetailCustomerViewModel by viewModel()
    private lateinit var mDetailRVAdapter: DetailCustomerRVAdapter
    private var isEdit: Boolean = true
    private var _projectId: String? = null
    private var _taskId: String? = null
    private var _customerInfo: CustomerResponse? = null
    private var _products: ArrayList<ProductResponse> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isEdit = requireArguments().getBoolean("isEdit")
        _projectId = requireArguments().getString("projectId")
        _taskId = requireArguments().getString("taskId")

        val _customerJson = requireArguments()?.getString("customerInfo")
        _customerJson?.let {
            _customerInfo = Gson().fromJson(_customerJson, CustomerResponse::class.java)
        }

        mDetailRVAdapter = DetailCustomerRVAdapter(
            promotions = arrayListOf(),
            gifts = arrayListOf(),
            images = arrayListOf(),
            context = requireContext(),
            customerName = _customerInfo?.name.toString(),
            customerPhone = _customerInfo?.mobileNumber.toString(),
            codeBill = _customerInfo?.customerBill.toString(),
            onItemPromotionSelectClick = { mPromotion, isChecked ->
                if (isChecked) {
                    mDetailRVAdapter.selectPromotionItem(mPromotion)
                    val items = ArrayList<ProductResponse>()
                    _products.forEach {
                        items.add(it.copy())
                    }
                    if (_products.size != 0) {
                        val dialog = SelectProductBottomSheetFragment(
                            items,
                            onSelectProductListener = { mProducts ->
                                mDetailRVAdapter.addAllProduct(mPromotion, mProducts)
                            })
                        dialog.show(requireActivity().supportFragmentManager, dialog.tag)
                    }
                } else {
                    mDetailRVAdapter.unSelectPromotionItem(mPromotion)
                }
            },
            onItemPromotionAddClick = { mPromotion ->
                var newQuantity = mPromotion.quantity + 1
                mDetailRVAdapter.updatePromotionQuantity(mPromotion, newQuantity)
            },
            onItemPromotionMinusClick = { mPromotion ->
                var newQuantity = mPromotion.quantity - 1
                if (newQuantity <= 0) {
                    newQuantity = 1
                }
                mDetailRVAdapter.updatePromotionQuantity(mPromotion, newQuantity)
            },
            onItemPromotionEditClick = { mPromotion ->
                val items = ArrayList<ProductResponse>()
                _products.forEach {
                    items.add(it.copy())
                }
                items.forEach { item ->
                    if (mPromotion.products.map { it.id.toString() }.contains(item.id)) {
                        val selectProduct = mPromotion.products.firstOrNull { mProducts ->
                            mProducts.id.equals(item.id)
                        }
                        item.isSelect = true
                        item.quantity = 1
                        selectProduct?.let {
                            item.quantity = it.quantity
                        }
                    }
                }
                if (items.size != 0) {
                    val dialog = SelectProductBottomSheetFragment(
                        items,
                        onSelectProductListener = { mProducts ->
                            mDetailRVAdapter.addAllProduct(mPromotion, mProducts)
                        })
                    dialog.show(requireActivity().supportFragmentManager, dialog.tag)
                }
            },
            isEdit = isEdit,
            onItemImageClick = {
                if (it.type == ImageAdapter.MEDIA_IMAGE) {
                    val imageDialog = ImageDialog(
                        title = it.note ?: "",
                        imageUrl = it.uri
                    )
                    imageDialog.show(childFragmentManager, imageDialog.tag)
                } else {
                    val videoDialog = VideoDialog(
                        title = it.note ?: "",
                        imageUrl = it.uri
                    )
                    videoDialog.show(childFragmentManager, videoDialog.tag)
                }
            }, onItemPromotionQuantityClick = { mPromotion ->
                val dialog = EditQuantityDialog(mPromotion, {
                    mDetailRVAdapter.updatePromotionQuantity(mPromotion, it)
                })
                dialog.show(childFragmentManager, dialog.tag)
            }
        )

        _taskId?.let {
            if (_customerInfo != null) {
                viewModel.getCustomerBill(
                    taskId = it,
                    billId = _customerInfo!!.customerBill.toString(),
                    projectId = _projectId!!.toString()
                )
                viewModel.fetchProducts(projectId = _projectId!!.toString())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        if (isEdit) {
            binding.imbIcFilter.visibility = View.VISIBLE
        } else {
            binding.imbIcFilter.visibility = View.INVISIBLE
        }

        binding.imbIcFilter.setSingleClick {
            _taskId?.let {
                if (_customerInfo != null) {
                    val request = mDetailRVAdapter.getSelectPromotions()
                    viewModel.addProductToBill(
                        taskId = _taskId.toString(),
                        billId = _customerInfo!!.customerBill.toString(),
                        request = request
                    )
                }
            }
        }

        binding.rvPage.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = mDetailRVAdapter
        }

        viewModel.customerBill.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            mDetailRVAdapter.addData(
                                mImages = it.first?.attachments?.map {
                                    MediaData(
                                        id = it.id.toString(),
                                        thumbnail = it.thumbnailUrl.toString(),
                                        uri = it.imageUrl.toString(),
                                        note = it.note.toString(),
                                        isCompleted = false,
                                        type = MediaAdapter.MEDIA_IMAGE
                                    )
                                } as ArrayList<MediaData>,
                                mPromotions = it.second?.data as ArrayList<PromotionResponse>,
                                mGift = it.third?.data as ArrayList<GiftResponse>,
                                mCodeBill = it.first?.code_bill.toString()
                            )
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
                            requireContext().showMessageDialog(message = it.message) {
                                findNavController().navigateUp()
                            }
                        }
                    }
                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.products.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            _products.clear()
                            _products.addAll(it)
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