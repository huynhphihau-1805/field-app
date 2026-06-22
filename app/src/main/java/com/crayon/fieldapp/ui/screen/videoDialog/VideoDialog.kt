package com.crayon.fieldapp.ui.screen.videoDialog

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.MediaController
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.DialogVideoBinding
import com.crayon.fieldapp.utils.setSingleClick


class VideoDialog(private var title: String, private val imageUrl: String) : DialogFragment() {

    private var _binding: DialogVideoBinding? = null
    private val binding get() = _binding!!

    private var downloadManager: DownloadManager? = null
    private var downloadImageId: Long = -1
    private var trackingStatusThread: Thread? = null

    @Volatile
    private var isDownloadCompleted = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogVideoBinding.inflate(inflater, container, false)
        activity?.let {
            downloadManager = it.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            it.registerReceiver(
                onCompleted,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED
            )
            it.registerReceiver(
                onNotificationClicked,
                IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED), Context.RECEIVER_EXPORTED
            )
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            pbLoading.visibility = View.VISIBLE
            toolbar.setNavigationIcon(resources.getDrawable(R.drawable.ic_left_arrow, null))
            toolbar.setNavigationOnClickListener { dismiss() }
            tvNote.text = title

            val uri: Uri = Uri.parse(imageUrl)
            val mediaController = MediaController(requireContext())
            videoView.setVideoURI(uri)
            mediaController.show()
            videoView.setMediaController(mediaController)
            videoView.start()

            videoView.setOnPreparedListener {
                pbLoading.visibility = View.GONE
            }

            videoView.setOnCompletionListener {
                Toast.makeText(
                    requireContext(),
                    "Thank You...!!!",
                    Toast.LENGTH_LONG
                ).show()
                pbLoading.visibility = View.GONE
            }

            videoView.setOnErrorListener { _, _, _ ->
                Toast.makeText(
                    requireContext(),
                    "Oops An Error Occur While Playing Video...!!!",
                    Toast.LENGTH_LONG
                ).show()
                pbLoading.visibility = View.GONE
                false
            }

            imgSave.setSingleClick {
                if (!verifyPermissions()) {
                    return@setSingleClick
                }
                val fileName: String = imageUrl.substring(imageUrl.lastIndexOf('/') + 1)
                startDownload(Uri.parse(imageUrl), fileName)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            it.setCanceledOnTouchOutside(true)
            val window = it.window
            val wlp = window!!.attributes
            wlp.gravity = Gravity.CENTER
            it.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.attributes = wlp
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val lp = window.attributes
            lp.dimAmount = 0.2f
            lp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            it.window?.attributes = lp
        }
    }

    override fun onPause() {
        super.onPause()
        binding.videoView.stopPlayback()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.let {
            it.unregisterReceiver(onCompleted)
            it.unregisterReceiver(onNotificationClicked)
            trackingStatusThread?.interrupt()
        }
        _binding = null
    }

    private var onCompleted: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            isDownloadCompleted = true
            binding.imgSave.isEnabled = true
            Toast.makeText(context, "Tải video thành công", Toast.LENGTH_LONG).show()
        }
    }

    private var onNotificationClicked: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Handle notification click
        }
    }

    private fun verifyPermissions(): Boolean {
        val permissionExternalMemory =
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        if (permissionExternalMemory != PackageManager.PERMISSION_GRANTED) {
            val STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(requireActivity(), STORAGE_PERMISSIONS, 1)
            return false
        }
        return true
    }

    private fun startDownload(uri: Uri, fileName: String) {
        isDownloadCompleted = false

        val request = DownloadManager.Request(uri)
        request.setTitle(fileName)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DCIM, fileName)

        downloadImageId = downloadManager?.enqueue(request) ?: -1
        startDownloadStatusTracking(downloadImageId)

        binding.imgSave.isEnabled = false
    }

    private fun startDownloadStatusTracking(downloadImageId: Long) {
        trackingStatusThread = Thread {
            while (!isDownloadCompleted) {
                activity?.runOnUiThread {
                    // Update UI for download status if needed
                }
                Thread.sleep(TRACKING_STATUS_DELAY)
            }
        }
        trackingStatusThread?.start()
    }

    companion object {
        private const val TRACKING_STATUS_DELAY = 500L
    }
}
