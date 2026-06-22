package com.crayon.fieldapp.ui.screen.detailTask.changeGift.receiveGift

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.R
import com.crayon.fieldapp.data.remote.response.GiftResponse
import com.crayon.fieldapp.databinding.FragmentImportGiftBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.ui.screen.detailTask.changeGift.receiveGift.adapter.ReceiveGiftAdapter
import com.crayon.fieldapp.utils.Status
import com.crayon.fieldapp.utils.Utils
import com.crayon.fieldapp.utils.setSingleClick
import com.crayon.fieldapp.utils.showMessageDialog
import com.example.moviedb.utils.getQueryTextChangeStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReceiveGiftFragment : BaseFragment<FragmentImportGiftBinding, ReceiveGiftViewModel>() {

    override val layoutId: Int = R.layout.fragment_import_gift
    override val viewModel: ReceiveGiftViewModel by viewModel()
    private lateinit var mGiftAdapter: ReceiveGiftAdapter
    private var _projectId: String? = null
    private var _taskId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _projectId = requireArguments()?.getString("projectId")
        _taskId = requireArguments()?.getString("taskId")

        mGiftAdapter =
            ReceiveGiftAdapter(
                arrayListOf(),
                requireContext(),
                onItemMinusListener = { mGift, position ->
                    var quantity = mGift.quantityIn - 1
                    if (quantity < 0) {
                        quantity = 0
                    }
                    mGiftAdapter.onUpdateQuantity(mGift, quantity, position)
                },
                onItemPlusListener = { mGift, position ->
                    var quantity = mGift.quantityIn + 1
                    mGiftAdapter.onUpdateQuantity(mGift, quantity, position)
                },
                onItemQuantityListener = { mGift ->
//                val dialog =
//                    EditQuantityGiftDialog(mGift, onUpdateQuantityClick = { mQuantity ->
//                        mGiftAdapter.onUpdateQuantity(mGift, mQuantity, position)
//                    })
//                dialog.show(requireActivity().supportFragmentManager, dialog.tag)
                })
        _taskId?.let {
            viewModel.fetchGifts(taskId = it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imbIcBack.setSingleClick {
            findNavController().navigateUp()
        }

        binding.imbIcFilter.setSingleClick {
            val gifts = mGiftAdapter.getSelectItems()
            if (gifts.size == 0) {
                requireContext().showMessageDialog(message = "Vui lòng chọn quà tặng")
                return@setSingleClick
            }
            _taskId?.let {
                viewModel.receiveGift(taskId = it, gift = gifts)
            }
        }

        setUpSearchStateFlow()

        viewModel.updateGift.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            requireContext().showMessageDialog(message = it) {
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

        viewModel.gifts.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {
                        binding.pbLoading.visibility = View.VISIBLE
                    }

                    Status.SUCCESS -> {
                        binding.pbLoading.visibility = View.GONE
                        it.data?.let {
                            mGiftAdapter.addAll(it as ArrayList<GiftResponse>)
                        }
                    }

                    Status.ERROR -> {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            }
        })
        binding.rvProduct.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = mGiftAdapter
        }
    }

    private fun setUpSearchStateFlow() {
        val iconSearchClose =
            binding.svProduct.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        iconSearchClose?.setSingleClick {
            val et =
                binding.svProduct.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
            et.setText("")
            binding.svProduct.setQuery("", false)
            mGiftAdapter.refresh()
            Utils.hideKeyboard(requireActivity())
        }

        binding.svProduct.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                mGiftAdapter.getFilter().filter(query.toString())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })

        GlobalScope.launch {
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
                            mGiftAdapter.filter.filter(result.toString())
                        }
                    }
            }
        }
    }
}