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
import com.crayon.fieldapp.databinding.DialogEditPriceBinding
import com.crayon.fieldapp.ui.widgets.MoneyTextWatcher
import com.crayon.fieldapp.ui.widgets.NumericKeyBoardTransformationMethod
import com.crayon.fieldapp.utils.Utils
import java.text.DecimalFormat

class EditPriceProductDialog(
    val product: ProductResponse,
    val onUpdatePriceClick: (Int) -> Unit = {}
) : DialogFragment() {

    private var _binding: DialogEditPriceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using ViewBinding
        _binding = DialogEditPriceBinding.inflate(inflater, container, false)
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

            // Set the transformation method for the price field
            binding.edtPrice.transformationMethod = NumericKeyBoardTransformationMethod()

            binding.btnCancel.setOnClickListener {
                dismiss()
            }

            binding.edtPrice.addTextChangedListener(object : MoneyTextWatcher(binding.edtPrice) {
                // You can add logic here if needed
            })

            binding.txtProductName.text = product.name
            val format = DecimalFormat("#,###")
            format.maximumFractionDigits = 0
            binding.txtCurrentPrice.text = format.format(product.price)

            binding.btnUpdate.setOnClickListener {
                val newPrice = binding.edtPrice.text.toString()
                onUpdatePriceClick.invoke(newPrice.replace(",", "").toInt())
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
