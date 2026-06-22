package com.crayon.fieldapp.ui.screen.imageDialog

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.DialogImageBinding
import com.crayon.fieldapp.utils.setSingleClick
import com.github.chrisbanes.photoview.OnSingleFlingListener
import java.io.IOException
import kotlin.math.abs


class ImageDialog(private var title: String, private val imageUrl: String) :
    DialogFragment() {

    private var _binding: DialogImageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup toolbar
        binding.toolbar.setNavigationIcon(R.drawable.ic_left_arrow)
        binding.toolbar.setNavigationOnClickListener { dismiss() }

        // Set dialog title
        binding.tvNote.text = title

        // Handle image saving
        binding.imgSave.setSingleClick {
            if (!verifyPermissions()) {
                return@setSingleClick
            }
            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val fileName: String = imageUrl.substring(imageUrl.lastIndexOf('/') + 1)
                        Toast.makeText(requireContext(), "Saving Image...", Toast.LENGTH_SHORT)
                            .show()
                        saveBitmap(
                            requireContext(),
                            bitmap = resource,
                            mimeType = "image/jpeg",
                            displayName = fileName,
                            format = Bitmap.CompressFormat.JPEG
                        )
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        // Handle fling gestures
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
                    if (abs(diffX) > abs(diffY)) {
                        if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            dismiss()
                            return true
                        }
                    } else if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        dismiss()
                        return true
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                return false
            }
        })

        // Load image into PhotoView
        binding.pbLoading.visibility = View.VISIBLE
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.pbLoading.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.pbLoading.visibility = View.GONE
                    return false
                }
            })
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.photoView.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            dialog.setCanceledOnTouchOutside(true)
            dialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                attributes = attributes.apply {
                    dimAmount = 0.2f
                    flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                    gravity = Gravity.CENTER
                }
            }
        }
    }

    private fun verifyPermissions(): Boolean {
        val permissionExternalMemory =
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        if (permissionExternalMemory != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
            return false
        }
        return true
    }

    @Throws(IOException::class)
    private fun saveBitmap(
        context: Context,
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        mimeType: String,
        displayName: String
    ): Uri {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        }

        var uri: Uri? = null

        return runCatching {
            with(context.contentResolver) {
                insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.also {
                    uri = it
                    openOutputStream(it)?.use { stream ->
                        if (!bitmap.compress(format, 95, stream))
                            throw IOException("Failed to save bitmap.")
                    } ?: throw IOException("Failed to open output stream.")
                } ?: throw IOException("Failed to create new MediaStore record.")
            }
        }.getOrElse {
            uri?.let { orphanUri -> context.contentResolver.delete(orphanUri, null, null) }
            throw it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }
}
