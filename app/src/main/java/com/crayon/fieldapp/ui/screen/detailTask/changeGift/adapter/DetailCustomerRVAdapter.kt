package com.crayon.fieldapp.ui.screen.detailTask.changeGift.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.request.AddPromotionGiftRequest
import com.crayon.fieldapp.data.remote.request.AddPromotionRequest
import com.crayon.fieldapp.data.remote.request.ProjectGiftRequest
import com.crayon.fieldapp.data.remote.request.ProjectProductRequest
import com.crayon.fieldapp.data.remote.response.GiftResponse
import com.crayon.fieldapp.data.remote.response.ProductResponse
import com.crayon.fieldapp.data.remote.response.PromotionResponse
import com.crayon.fieldapp.databinding.ItemBillInfoBinding
import com.crayon.fieldapp.databinding.ItemCustomerInfoBinding
import com.crayon.fieldapp.databinding.ItemGiftInfoBinding
import com.crayon.fieldapp.databinding.ItemPromotionInfoBinding
import com.crayon.fieldapp.ui.screen.detailTask.adapter.MediaData

class DetailCustomerRVAdapter constructor(
    private val images: ArrayList<MediaData>,
    private val promotions: ArrayList<PromotionResponse>,
    private val gifts: ArrayList<GiftResponse>,
    var customerName: String,
    var customerPhone: String,
    var codeBill: String,
    private val context: Context,
    private val onItemPromotionSelectClick: (item: PromotionResponse, isChecked: Boolean) -> Unit = { _, _ -> },
    private val onItemPromotionAddClick: (item: PromotionResponse) -> Unit = { },
    private val onItemPromotionEditClick: (item: PromotionResponse) -> Unit = { },
    private val onItemPromotionMinusClick: (item: PromotionResponse) -> Unit = { },
    private val onItemPromotionQuantityClick: (item: PromotionResponse) -> Unit = { },
    private val onItemImageClick: (item: MediaData) -> Unit = { },
    private val isEdit: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mImageAdapter: BillImageRVAdapter
    private lateinit var mPromotionRVAdapter: PromotionRVAdapter
    private lateinit var mGiftRVAdapter: GiftRVAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_customer_info -> {
                val binding = ItemCustomerInfoBinding.inflate(inflater, parent, false)
                CustomerItemViewHolder(binding)
            }

            R.layout.item_bill_info -> {
                val binding = ItemBillInfoBinding.inflate(inflater, parent, false)
                BillItemViewHolder(binding)
            }

            R.layout.item_promotion_info -> {
                val binding = ItemPromotionInfoBinding.inflate(inflater, parent, false)
                PromotionItemViewHolder(binding)
            }

            else -> {
                val binding = ItemGiftInfoBinding.inflate(inflater, parent, false)
                GiftItemViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CustomerItemViewHolder -> {
                holder.binding.txtCustomerName.text = customerName
                holder.binding.txtCustomerPhone.text = customerPhone
            }

            is BillItemViewHolder -> {
                holder.binding.txtBill.text = codeBill.replace("\"", "")
                mImageAdapter = BillImageRVAdapter(images, context, onItemImageClick)
                holder.binding.rvImages.apply {
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = mImageAdapter
                }
            }

            is PromotionItemViewHolder -> {
                mPromotionRVAdapter = PromotionRVAdapter(
                    items = promotions,
                    context = context,
                    onCheckBoxSelect = { promotion, isChecked ->
                        if (isChecked) {
                            onItemPromotionSelectClick(promotion, isChecked)
                            mPromotionRVAdapter.onSelectItem(promotion)
                        } else {
                            mPromotionRVAdapter.onUnSelectItem(promotion)
                        }
                    },
                    onItemDeleteListener = { mPromotion ->
                        unSelectPromotionItem(mPromotion)
                        mPromotionRVAdapter.onDeleteAllProduct(mPromotion)
                    },
                    onItemPlusListener = { onItemPromotionAddClick(it) },
                    onItemMinusListener = { onItemPromotionMinusClick(it) },
                    onItemEditListener = { onItemPromotionEditClick(it) },
                    onItemQuantityListener = { onItemPromotionQuantityClick(it) },
                    isEdit = isEdit
                )
                holder.binding.rvPromotion.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = mPromotionRVAdapter
                }
            }

            is GiftItemViewHolder -> {
                mGiftRVAdapter = GiftRVAdapter(
                    items = gifts,
                    context = context,
                    onItemSelectedListener = { mGift, isChecked ->
                        if (isChecked) mGiftRVAdapter.onSelectItem(mGift)
                        else mGiftRVAdapter.onUnSelectItem(mGift)
                    },
                    onItemMinusListener = { mGift ->
                        val quantity = (mGift.selectQuantity - 1).coerceAtLeast(0)
                        mGiftRVAdapter.onUpdateQuantity(mGift, quantity)
                    },
                    onItemPlusListener = { mGift ->
                        val quantity = mGift.selectQuantity + 1
                        mGiftRVAdapter.onUpdateQuantity(mGift, quantity)
                    },
                    isEdit = isEdit
                )
                holder.binding.rvGift.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = mGiftRVAdapter
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> R.layout.item_customer_info
            1 -> R.layout.item_bill_info
            2 -> R.layout.item_promotion_info
            else -> R.layout.item_gift_info
        }
    }

    override fun getItemCount(): Int = 4

    inner class CustomerItemViewHolder(val binding: ItemCustomerInfoBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class BillItemViewHolder(val binding: ItemBillInfoBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class PromotionItemViewHolder(val binding: ItemPromotionInfoBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class GiftItemViewHolder(val binding: ItemGiftInfoBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun addData(
        mImages: ArrayList<MediaData>,
        mPromotions: ArrayList<PromotionResponse>,
        mGift: ArrayList<GiftResponse>,
        mCodeBill: String

    ) {
        images.clear()
        images.addAll(mImages)

        promotions.clear()
        promotions.addAll(mPromotions)

        gifts.clear()
        gifts.addAll(mGift)
        codeBill = mCodeBill

        notifyDataSetChanged()
    }

    fun updatePromotionQuantity(item: PromotionResponse, quantity: Int) {
        promotions.indexOfFirst { it.id.toString().equals(item.id) }.let { index ->
            if (index != -1) {
                promotions.get(index).quantity = quantity
                notifyItemChanged(2)
            }
        }
    }

    fun updateGiftQuantity(item: GiftResponse, quantity: Int) {
        gifts.indexOfFirst { it.id.toString().equals(item.id) }.let { index ->
            if (index != -1) {
                gifts.get(index).quantity = quantity
                notifyItemChanged(3)
            }
        }
    }

    fun selectPromotionItem(data: PromotionResponse) {
        promotions.findLast { it.id.equals(data.id) }?.let {
            it.isSelect = true
            if (it.quantity < 1) {
                it.quantity = 1
            }
            notifyItemChanged(2)
        }
    }

    fun unSelectPromotionItem(data: PromotionResponse) {
        promotions.findLast { it.id.equals(data.id) }?.let {
            it.isSelect = false
            it.quantity = 0
            notifyItemChanged(2)
        }
    }

    fun selectGiftItem(data: GiftResponse) {
        gifts.findLast { it.id.equals(data.id) }?.let {
            it.isSelect = true
            if (it.quantity < 1) {
                it.quantity = 1
            }
            notifyItemChanged(3)
        }
    }

    fun unSelectGiftItem(data: GiftResponse) {
        gifts.findLast { it.id.equals(data.id) }?.let {
            it.isSelect = false
            it.quantity = 0
            notifyItemChanged(3)
        }
    }

    fun addAllProduct(mPromotion: PromotionResponse, mProduct: ArrayList<ProductResponse>) {
        promotions.indexOfFirst { it.id.toString().equals(mPromotion.id) }.let { index ->
            if (index != -1) {
                promotions.get(index).products.clear()
                promotions.get(index).products.addAll(mProduct)
                if (mProduct.size == 0) {
                    promotions.get(index).quantity = 0
                    promotions.get(index).isSelect = false
                }
                notifyItemChanged(2)
            }
        }
    }

    fun getSelectPromotions(): AddPromotionGiftRequest {
        var mGifts = gifts.filter { it.isSelect == true }.map {
            ProjectGiftRequest(
                quantity = it.selectQuantity,
                giftId = it.id.toString()
            )
        } as ArrayList<ProjectGiftRequest>
        var mPromotions = promotions.filter { it.isSelect == true }.map { mPromotionSelect ->
            AddPromotionRequest(promotionId = mPromotionSelect.id.toString(),
                products = mPromotionSelect.products.map { mProduct ->
                    ProjectProductRequest(
                        productId = mProduct.id.toString(),
                        price = mProduct.price,
                        quantity = mProduct.quantity
                    )
                } as ArrayList<ProjectProductRequest>,
                quantity = mPromotionSelect.quantity
            )
        } as ArrayList
        return AddPromotionGiftRequest(promotions = mPromotions, gifts = mGifts)
    }
}
