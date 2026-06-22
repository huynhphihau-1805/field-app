package com.crayon.fieldapp.ui.screen.detailTask.changeGift.step4.adapter

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
import com.crayon.fieldapp.databinding.ItemGiftInfoBinding
import com.crayon.fieldapp.databinding.ItemPromotionInfoBinding
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.adapter.GiftRVAdapter
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.adapter.PromotionRVAdapter

class SelectPromotionRVAdapter(
    val promotion: ArrayList<PromotionResponse>,
    val gifts: ArrayList<GiftResponse>,
    val context: Context,
    val onShowSelectProduct: (promotion: PromotionResponse) -> Unit = {},
    val onItemClick: (String) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mPromotionRVAdapter: PromotionRVAdapter
    private lateinit var mGiftRVAdapter: GiftRVAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_promotion_info -> {
                val binding = ItemPromotionInfoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PromotionItemViewHolder(binding)
            }

            else -> {
                val binding =
                    ItemGiftInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                GiftItemViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PromotionItemViewHolder -> {
                mPromotionRVAdapter = PromotionRVAdapter(
                    items = promotion,
                    context = context,
                    onCheckBoxSelect = { mPromotion, isChecked ->
                        if (isChecked) {
                            mPromotionRVAdapter.onSelectItem(mPromotion)
                            onShowSelectProduct(mPromotion)
                        } else {
                            mPromotionRVAdapter.onUnSelectItem(mPromotion)
                        }
                    },
                    onItemDeleteListener = { mPromotion ->
                        mPromotionRVAdapter.onDeleteAllProduct(promotion = mPromotion)
                    },
                    onItemPlusListener = { mPromotion ->
                        var quantity = mPromotion.quantity + 1
                        mPromotionRVAdapter.onUpdateQuantity(mPromotion, quantity)
                    },
                    onItemMinusListener = { mPromotion ->
                        var quantity = mPromotion.quantity - 1
                        if (quantity <= 0) {
                            quantity = 1
                        }
                        mPromotionRVAdapter.onUpdateQuantity(mPromotion, quantity)
                    }
                )
                holder.binding.rvPromotion.apply {
                    layoutManager = LinearLayoutManager(context)
                    this.adapter = mPromotionRVAdapter
                }
            }

            is GiftItemViewHolder -> {
                mGiftRVAdapter = GiftRVAdapter(
                    items = gifts,
                    context = context,
                    onItemSelectedListener = { mGift, isChecked ->
                        if (isChecked) {
                            mGiftRVAdapter.onSelectItem(mGift)
                        } else {
                            mGiftRVAdapter.onUnSelectItem(mGift)
                        }
                    },
                    onItemMinusListener = { mGift ->
                        var quantity = mGift.selectQuantity - 1
                        if (quantity < 0) {
                            quantity = 0
                        }
                        mGiftRVAdapter.onUpdateQuantity(mGift, quantity)
                    },
                    onItemPlusListener = { mGift ->
                        var quantity = mGift.selectQuantity + 1
                        mGiftRVAdapter.onUpdateQuantity(mGift, quantity)
                    }
                )
                holder.binding.rvGift.apply {
                    layoutManager = LinearLayoutManager(context)
                    this.adapter = mGiftRVAdapter
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> R.layout.item_promotion_info
            else -> R.layout.item_gift_info
        }
    }

    inner class PromotionItemViewHolder(val binding: ItemPromotionInfoBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class GiftItemViewHolder(val binding: ItemGiftInfoBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun addItems(mPromotion: ArrayList<PromotionResponse>, mGift: ArrayList<GiftResponse>) {
        promotion.clear()
        gifts.clear()
        promotion.addAll(mPromotion)
        gifts.addAll(mGift)
        notifyDataSetChanged()
    }

    fun addAllProduct(mPromotion: PromotionResponse, mProduct: ArrayList<ProductResponse>) {
        promotion.indexOfFirst { it.id.toString().equals(mPromotion.id) }.let { index ->
            if (index != -1) {
                promotion[index].products.clear()
                promotion[index].products.addAll(mProduct)
                notifyItemChanged(0)
            }
        }
    }

    fun getSelectPromotions(): AddPromotionGiftRequest {
        val mGifts = gifts.filter { it.isSelect == true }.map {
            ProjectGiftRequest(
                quantity = it.selectQuantity,
                giftId = it.id.toString()
            )
        } as ArrayList<ProjectGiftRequest>

        val mPromotions = promotion.filter { it.isSelect == true }.map { mPromotion ->
            AddPromotionRequest(
                promotionId = mPromotion.id.toString(),
                products = mPromotion.products.map { mProduct ->
                    ProjectProductRequest(
                        productId = mProduct.id.toString(),
                        price = mProduct.price,
                        quantity = mProduct.quantity
                    )
                } as ArrayList<ProjectProductRequest>,
                quantity = mPromotion.quantity
            )
        } as ArrayList

        return AddPromotionGiftRequest(promotions = mPromotions, gifts = mGifts)
    }

    override fun getItemCount(): Int {
        return 2
    }
}
