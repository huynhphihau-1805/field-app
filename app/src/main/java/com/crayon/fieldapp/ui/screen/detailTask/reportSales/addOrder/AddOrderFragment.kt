package com.crayon.fieldapp.ui.screen.detailTask.reportSales.addOrder

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.OrderResponse
import com.crayon.fieldapp.data.remote.response.ProductResponse
import com.crayon.fieldapp.databinding.FragmentAddOrderBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailTask.reportSales.adapter.SelectProductRVAdapter
import com.crayon.fieldapp.ui.screen.detailTask.reportSales.addOrder.dialog.EditPriceProductDialog
import com.crayon.fieldapp.ui.screen.detailTask.reportSales.addOrder.dialog.EditQuantityProductDialog
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.Utils
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showMessageDialog
import com.example.moviedb.utils.getQueryTextChangeStateFlow
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel


class AddOrderFragment : BaseFragment<FragmentAddOrderBinding, AddOrderViewModel>() {

    override val layoutId: Int = R.layout.fragment_add_order
    override val viewModel: AddOrderViewModel by viewModel()
    private lateinit var mProductAdapter: SelectProductRVAdapter
    private var projectId: String? = null
    private var taskId: String? = null
    private var _orderResponse: OrderResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectId = arguments?.getString("projectId").toString()
        taskId = arguments?.getString("taskId").toString()
        val order = arguments?.getString("order")
        order?.let {
            _orderResponse = Gson().fromJson(it, OrderResponse::class.java)
        }
        viewModel.fetchProducts(projectId.toString())

        mProductAdapter =
            SelectProductRVAdapter(
                items = arrayListOf(),
                onItemSelectClick = { mProduct, isChecked ->
                    if (isChecked) {
                        mProductAdapter.selectItem(mProduct)
                    } else {
                        mProductAdapter.unSelectItem(mProduct)
                    }
                },
                onPriceClick = { mProduct ->
                    val dialog =
                        EditPriceProductDialog(mProduct, onUpdatePriceClick = { mPrice ->
                            viewModel.updatePrice(product = mProduct, price = mPrice.toLong())
                            mProductAdapter.updatePrice(item = mProduct, price = mPrice)
                        })
                    dialog.show(requireActivity().supportFragmentManager, dialog.tag)
                },
                context = requireContext(),
                onItemAddClick = { mProduct ->
                    var newQuantity = mProduct.quantity + 1
                    mProductAdapter.updateQuantity(mProduct, newQuantity)
                },
                onItemMinusClick = { mProduct ->
                    var newQuantity = mProduct.quantity - 1
                    if (newQuantity <= 0) {
                        newQuantity = 1
                    }
                    mProductAdapter.updateQuantity(mProduct, newQuantity)
                },
                onItemQuantityClick = { mProduct ->
                    val dialog = EditQuantityProductDialog(mProduct, {
                        mProductAdapter.updateQuantity(mProduct, it)
                    })
                    dialog.show(childFragmentManager, dialog.tag)
                }
            )

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (_orderResponse != null) {
            binding.tvTitle.text = "Cập nhật đơn hàng"
        } else {
            binding.tvTitle.text = "Tạo đơn hàng"
        }

        binding.cbSelectAll.setOnClickListener {
            val isChecked = binding.cbSelectAll.isChecked
            if (isChecked) {
                mProductAdapter.selectAll()
            } else {
                mProductAdapter.unSelectAll()
            }
        }

        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.imbIcFilter.setSingleClick {
            val products = mProductAdapter.getAllItemSelected()
            if (products.size == 0) {
                requireContext().showMessageDialog(message = "Vui lòng chọn sản phẩm") {}
                return@setSingleClick
            } else {
                if (_orderResponse != null) {
                    viewModel.updateOrder(
                        taskId = taskId.toString(),
                        orderId = _orderResponse!!.id.toString(),
                        listProducts = products
                    )
                } else {
                    viewModel.addOrder(
                        taskId = taskId.toString(), listProducts = products
                    )
                }

            }
        }

        setUpSearchStateFlow()

        binding.rvProduct.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = mProductAdapter
        }

        viewModel.products.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            if (_orderResponse != null) {
                                it.forEach { mProjectOfProduct ->
                                    if (_orderResponse!!.products.map { it.id }
                                            .contains(mProjectOfProduct.id)) {
                                        mProjectOfProduct.isSelect = true
                                        mProjectOfProduct.quantity =
                                            _orderResponse!!.products.findLast {
                                                it.id.equals(
                                                    mProjectOfProduct.id
                                                )
                                            }!!.quantity
                                    }
                                }
                                mProductAdapter.addAll(it as ArrayList<ProductResponse>)
                            } else {
                                mProductAdapter.addAll(it as ArrayList<ProductResponse>)
                            }
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.addOrder.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let { mOrder ->
                            requireContext().showMessageDialog(message = "Tạo đơn hàng thành công") {
                                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                    "isNew",
                                    true
                                )
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


        viewModel.updateOrder.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let { mOrder ->
                            requireContext().showMessageDialog(message = "Cập nhật đơn hàng thành công") {
                                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                    "isNew",
                                    true
                                )
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
    }


    private fun setUpSearchStateFlow() {
        val iconSearchClose =
            binding.svProduct.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        iconSearchClose?.setSingleClick {
            val et =
                binding.svProduct.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
            et.setText("")
            binding.svProduct.setQuery("", false)
            mProductAdapter.refresh()
            Utils.hideKeyboard(requireActivity())
        }

        binding.svProduct.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                mProductAdapter.getFilter().filter(query.toString())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })

        viewLifecycleOwner.lifecycleScope.launch {
            binding.svProduct.let {
                it.getQueryTextChangeStateFlow()
                    .debounce(1000)
                    .filter { query ->
                        if (query.isEmpty()) {
                            return@filter false
                        } else {
                            return@filter true
                        }
                    }
                    .distinctUntilChanged()
                    .collect { result ->
                        withContext(Dispatchers.Main) {
//                            sv_product?.clearFocus()
                            mProductAdapter.getFilter().filter(result)
                        }
                    }
            }
        }
    }

}