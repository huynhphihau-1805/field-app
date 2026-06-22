package com.crayon.fieldapp.ui.screen.detailTask.reportSales.addOrder.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.crayon.fieldapp.data.remote.response.ProductResponse
import com.crayon.fieldapp.databinding.DialogEditQuantityBinding
import com.crayon.fieldapp.ui.widgets.NumericKeyBoardTransformationMethod
import com.crayon.fieldapp.utils.Utils

class EditQuantityProductDialog(
    val gift: ProductResponse,
    val onUpdateQuantityClick: (Int) -> Unit = {}
) : DialogFragment() {

    private var _binding: DialogEditQuantityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogEditQuantityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(true)
            val window = dialog.window
            val wlp = window!!.attributes
            wlp.gravity = Gravity.CENTER
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.attributes = wlp
            window.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            binding.edtPrice.transformationMethod = NumericKeyBoardTransformationMethod()

            binding.btnCancel.setOnClickListener {
                dismiss()
            }

            binding.btnUpdate.setOnClickListener {
                val newPrice = binding.edtPrice.text.toString()
                onUpdateQuantityClick.invoke(newPrice.replace(",", "").toInt())
                dismiss()
            }
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

