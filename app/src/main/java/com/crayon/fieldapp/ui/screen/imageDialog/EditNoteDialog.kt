package com.crayon.fieldapp.ui.screen.imageDialog

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.DialogAddNoteBinding
import com.crayon.fieldapp.utils.setSingleClick
import com.github.chrisbanes.photoview.OnSingleFlingListener


class EditNoteDialog(
    private var index: Int,
    private var note: String? = null,
    private val imageUrl: String
) : DialogFragment() {

    private var _binding: DialogAddNoteBinding? = null
    private val binding get() = _binding!!

    var mListener: EditNoteListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set initial note if available
        note?.let {
            binding.edtNote.setText(it)
        }

        // Handle back button click
        binding.imbIcBack.setSingleClick {
            dismiss()
        }

        // Handle save button click
        binding.imbIcSave.setSingleClick {
            val note = binding.edtNote.text.toString()
            mListener?.onEditNote(note, index)
            dismiss()
        }

        // Set fling listener for photo view
        binding.photoView.setOnSingleFlingListener(object : OnSingleFlingListener {
            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                try {
                    val diffY: Float = e2.y - e1.y
                    val diffX: Float = e2.x - e1.x
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            dismiss()
                            return true
                        }
                    } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        dismiss()
                        return true
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                return false
            }
        })

        // Load image using Glide
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.photoView.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(true)
            val window = dialog.window
            val wlp = window!!.attributes
            wlp.gravity = Gravity.CENTER
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.attributes = wlp
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val lp = window.attributes
            lp.dimAmount = 0.2f
            lp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dialog.window?.attributes = lp
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface EditNoteListener {
        fun onEditNote(note: String, index: Int)
    }

    fun setEditNoteListener(listener: EditNoteListener) {
        mListener = listener
    }

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }
}
