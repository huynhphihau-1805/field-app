package com.crayon.fieldapp.ui.base.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.crayon.fieldapp.R


class OtpSuccessDialog(val onConfirmButtonClick: (() -> Unit)) : DialogFragment() {
    lateinit var btnConfirm: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.dialog_otp_success, container, false).apply {
            btnConfirm = findViewById(R.id.btn_confirm)
            btnConfirm.setOnClickListener {
                this@OtpSuccessDialog.dismiss()
            }
        }


    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            // Set gravity of dialog
            dialog.setCanceledOnTouchOutside(false)
            val window = dialog.window
            val wlp = window!!.attributes
            wlp.gravity = Gravity.CENTER
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.attributes = wlp
            window.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val lp = window.attributes
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onConfirmButtonClick.invoke()
    }
}