package com.crayon.fieldapp.ui.base.dialog.filterStore

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.crayon.fieldapp.databinding.DialogFilterStoreBinding
import com.crayon.fieldapp.ui.base.dialog.filterStore.model.ItemStore
import com.example.moviedb.utils.getQueryTextChangeStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import studio.phillip.yolo.presentation.add.selectProject.adapter.FilterStoreRVAdapter

class FilterStoreDialog(
    private val onFilterStoreDialogListener: (ArrayList<String>) -> Unit = {},
    private val onClearFilter: () -> Unit = {}
) : DialogFragment() {

    private lateinit var binding: DialogFilterStoreBinding
    private lateinit var adapter: FilterStoreRVAdapter
    private var listStore: ArrayList<ItemStore>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFilterStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnChoose.setOnClickListener {
            val stores = adapter.getSelectedItem()
            stores?.let {
                onFilterStoreDialogListener(it as ArrayList<String>)
            }
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnClearAll.setOnClickListener {
            onClearFilter.invoke()
            dismiss()
        }

        binding.cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                adapter.selectAll()
            } else {
                adapter.clearAll()
            }
        }

        arguments?.let {
            listStore = it.getSerializable("store") as? ArrayList<ItemStore>
            listStore?.let { storeList ->
                adapter = FilterStoreRVAdapter(storeList, requireContext()) { item ->
                    if (item.isSelect) {
                        adapter.unSelectItem(item)
                    } else {
                        adapter.selectItem(item)
                    }
                }
                binding.rvStore.layoutManager = LinearLayoutManager(requireContext())
                binding.rvStore.adapter = adapter
            }
        }

        setUpSearchStateFlow()
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            setCanceledOnTouchOutside(true)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val params = attributes
                params.gravity = Gravity.CENTER
                attributes = params
                setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }
    }

    private fun setUpSearchStateFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.svStore.getQueryTextChangeStateFlow()
                .debounce(1000)
                .filter { query -> query.isNotEmpty() }
                .distinctUntilChanged()
                .collect { query ->
                    withContext(Dispatchers.Main) {
                        binding.svStore.clearFocus()
                        adapter.filter.filter(query)
                    }
                }
        }
    }
}
