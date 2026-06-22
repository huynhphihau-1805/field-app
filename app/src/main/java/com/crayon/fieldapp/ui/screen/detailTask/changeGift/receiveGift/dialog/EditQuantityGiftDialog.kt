package com.crayon.fieldapp.ui.screen.detailTask.changeGift.receiveGift.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.crayon.fieldapp.data.remote.response.GiftResponse
import com.crayon.fieldapp.databinding.DialogEditQuantityBinding
import com.crayon.fieldapp.ui.widgets.MoneyTextWatcher
import com.crayon.fieldapp.ui.widgets.NumericKeyBoardTransformationMethod
import com.crayon.fieldapp.utils.Utils

class EditQuantityGiftDialog(
    private val gift: GiftResponse,
    private val onUpdateQuantityClick: (Int) -> Unit = {}
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

        dialog?.apply {
            setCanceledOnTouchOutside(true)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                attributes = attributes.apply {
                    gravity = Gravity.CENTER
                }
                setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        binding.apply {
            edtPrice.transformationMethod = NumericKeyBoardTransformationMethod()

            btnCancel.setOnClickListener {
                this@EditQuantityGiftDialog.dismiss()
            }

            btnUpdate.setOnClickListener {
                val newPrice = edtPrice.text.toString()
                onUpdateQuantityClick.invoke(newPrice.replace(",", "").toIntOrNull() ?: 0)
                this@EditQuantityGiftDialog.dismiss()
            }

            edtPrice.addTextChangedListener(object : MoneyTextWatcher(edtPrice) {})
            txtProductName.text = gift.name
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
