package com.crayon.fieldapp.ui.screen.detailTask.changeGift.selectProduct

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.ProductResponse
import com.crayon.fieldapp.databinding.DialogSelectProductBinding
import com.crayon.fieldapp.ui.screen.detailTask.reportSales.adapter.SelectProductRVAdapter
import com.crayon.fieldapp.ui.screen.detailTask.reportSales.addOrder.dialog.EditPriceProductDialog
import com.crayon.fieldapp.utils.Utils
import com.crayon.fieldapp.utils.setSingleClick
import com.example.moviedb.utils.getQueryTextChangeStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SelectProductBottomSheetFragment(
    private val product: ArrayList<ProductResponse>,
    private val onSelectProductListener: (ArrayList<ProductResponse>) -> Unit = {},
    private val onUpdatePriceListener: (product: ProductResponse, price: Int) -> Unit = { product, price -> }
) : DialogFragment() {

    private var _binding: DialogSelectProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var mProductAdapter: SelectProductRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogSelectProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        dialog?.apply {
            setCanceledOnTouchOutside(true)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                attributes = attributes.apply {
                    gravity = Gravity.BOTTOM
                }
                setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            binding.apply {
                cbSelectAll.setOnClickListener {
                    val isChecked = cbSelectAll.isChecked
                    if (isChecked) {
                        mProductAdapter.selectAll()
                    } else {
                        mProductAdapter.unSelectAll()
                    }
                }

                btnCancel.setOnClickListener {
                    dismiss()
                }
                val iconSearchClose = svProduct.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
                iconSearchClose?.setSingleClick {
                    val et = svProduct.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
                    et.setText("")
                    svProduct.setQuery("", false)
                    mProductAdapter.refresh()
                    Utils.hideKeyboard(requireActivity())
                }

                svProduct.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                    androidx.appcompat.widget.SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        mProductAdapter.getFilter().filter(query.toString())
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return true
                    }
                })
            }

            viewLifecycleOwner.lifecycleScope.launch {
                binding.svProduct.getQueryTextChangeStateFlow()
                    .debounce(1000)
                    .filter { query -> query.isNotEmpty() }
                    .distinctUntilChanged()
                    .collect { result ->
                        withContext(Dispatchers.Main) {
                            mProductAdapter.getFilter().filter(result)
                        }
                    }
            }

            binding.rvProduct.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = mProductAdapter
            }

            mProductAdapter = SelectProductRVAdapter(
                items = product,
                onItemSelectClick = { mProduct, isChecked ->
                    if (isChecked) {
                        mProductAdapter.selectItem(mProduct)
                    } else {
                        mProductAdapter.unSelectItem(mProduct)
                    }
                },
                onPriceClick = { mProduct ->
                    val dialog = EditPriceProductDialog(mProduct, onUpdatePriceClick = { mPrice ->
                        onUpdatePriceListener(mProduct, mPrice)
                        mProductAdapter.updatePrice(mProduct, mPrice)
                    })
                    dialog.show(requireActivity().supportFragmentManager, dialog.tag)
                },
                context = requireContext(),
                onItemAddClick = { mProduct ->
                    val newQuantity = mProduct.quantity + 1
                    mProductAdapter.updateQuantity(mProduct, newQuantity)
                },
                onItemMinusClick = { mProduct ->
                    var newQuantity = mProduct.quantity - 1
                    if (newQuantity <= 0) {
                        newQuantity = 1
                    }
                    mProductAdapter.updateQuantity(mProduct, newQuantity)
                }
            )
        }
    }

    override fun onStop() {
        super.onStop()
        Utils.hideKeyboard(requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
