package com.crayon.fieldapp.ui.screen.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.Image
import android.media.ImageReader
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import android.util.SparseIntArray
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.crayon.fieldapp.R
import com.crayon.fieldapp.databinding.FragmentCameraBinding
import com.crayon.fieldapp.ui.base.BaseFragment
import com.crayon.fieldapp.utils.PermissionChecker
import com.crayon.fieldapp.utils.setSingleClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

class CameraFragment : BaseFragment<FragmentCameraBinding, CameraViewModel>() {

    override val layoutId: Int = R.layout.fragment_camera
    override val viewModel: CameraViewModel by viewModel()
    private val permissionChecker = PermissionChecker()
    private var isPermissionRequested = false
    private val mandatoryPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )


    private var isRecording = false
    private lateinit var mediaRecorder: MediaRecorder
    private var cameraDevice: CameraDevice? = null
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var imageReader: ImageReader
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private var isTakeImage: Boolean = false
    private var timer: CountDownTimer? = null

    private val cameraId: String by lazy {
        val cameraManager =
            requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.cameraIdList.first { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isTakeImage = requireArguments().getBoolean("isTakeImage", false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_SENSOR

        if (isTakeImage) {
            binding.tvTime.visibility = View.GONE
            binding.cameraCaptureButton.setSingleClick {
                takePicture()
            }
        } else {
            binding.tvTime.visibility = View.VISIBLE
            timer = object : CountDownTimer(30000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.tvTime.setText("Thời gian clip còn:" + "00:" + millisUntilFinished / 1000)
                }

                @SuppressLint("RestrictedApi")
                override fun onFinish() {
//                    videoCapture.stopRecording()
                }
            }

            binding.cameraCaptureButton.setOnClickListener {
                if (isRecording) {
                    binding.icSwitchCamera.visibility = View.VISIBLE
                    timer?.cancel()
                    stopRecording()
                    it.isSelected = false
                    isRecording = false
                } else {
                    timer?.start()
                    startRecording()
                    it.isSelected = true
                    isRecording = true
                    binding.icSwitchCamera.visibility = View.GONE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        checkCameraPermission()
    }

    override fun onPause() {
        stopBackgroundThread()
        closeCamera()
        super.onPause()
    }

    private fun startRecording() {
        val outputFile = File(
            requireContext().externalCacheDir,
            "video_${System.currentTimeMillis()}.mp4"
        )
        initMediaRecorder(outputFile)

        cameraDevice?.let { camera ->
            val surfaces = mutableListOf<Surface>()
            val previewSurface = binding.previewView.holder.surface
            surfaces.add(previewSurface)
            surfaces.add(mediaRecorder.surface)

            val captureRequestBuilder =
                camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
                    addTarget(previewSurface)
                    addTarget(mediaRecorder.surface)
                }

            camera.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    cameraCaptureSession = session
                    mediaRecorder.start()
                    isRecording = true
//                    binding.cameraRecordButton.text = "Stop Recording"
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to start recording",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, backgroundHandler)
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder.stop()
            mediaRecorder.reset()
            isRecording = false
//            binding.cameraRecordButton.text = "Record Video"
            val videoFile = File(
                requireContext().externalCacheDir,
                "recorded_video_${System.currentTimeMillis()}.mp4"
            )
            val videoUri = Uri.fromFile(videoFile)


            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                "url",
                videoUri.path
            )
            findNavController().navigateUp()
            Toast.makeText(requireContext(), "Video saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun checkCameraPermission() {
        permissionChecker.verifyPermissions(requireContext(),
            mandatoryPermissions,
            object : PermissionChecker.VerifyPermissionsCallback {
                override fun onPermissionAllGranted() {
                    setUpCamera()
                }

                override fun onPermissionDeny(permissions: Array<String>?) {
                    Toast.makeText(
                        context,
                        getString(R.string.PERMISSION_NOT_GRANTED),
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }

                override fun requestPermissionsCallback(
                    permissions: Array<String>?, requestCode: Int
                ) {
                    isPermissionRequested = true
                    requestFragmentPermission(permissions)
                }
            })

    }

    private fun initMediaRecorder(outputFile: File) {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(outputFile.absolutePath)
            setVideoEncodingBitRate(10000000)
            setVideoFrameRate(30)
            setVideoSize(1920, 1080)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOrientationHint(getJpegOrientation())
            prepare()
        }
    }


    private fun setUpCamera() {
        binding.previewView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                openCamera(holder)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                closeCamera()
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(holder: SurfaceHolder) {
        val cameraManager =
            requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    startPreview(holder)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                }
            }, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun startPreview(holder: SurfaceHolder) {
        cameraDevice?.let { camera ->
            val surface = holder.surface
            imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1)
            imageReader.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                saveImage(image)
            }, backgroundHandler)

            val surfaces = listOf(surface, imageReader.surface)
            val captureRequestBuilder =
                camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                    addTarget(surface)
                    set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation())
                }

            camera.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    cameraCaptureSession = session
                    cameraCaptureSession.setRepeatingRequest(
                        captureRequestBuilder.build(),
                        null,
                        backgroundHandler
                    )
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Toast.makeText(
                        requireContext(),
                        "Camera preview setup failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, backgroundHandler)
        }
    }

    private fun takePicture() {
        cameraDevice?.let { camera ->
            val captureRequestBuilder =
                camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                    addTarget(imageReader.surface)
                    set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation())
                }
            cameraCaptureSession.capture(captureRequestBuilder.build(), null, backgroundHandler)
        }
    }

    private fun saveImage(image: Image) {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        // Lấy góc xoay từ CameraCharacteristics
        val rotation = requireContext().getSystemService(Context.CAMERA_SERVICE)
            .let { it as CameraManager }
            .getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0

        // Xoay ảnh
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )

        // Ghi ảnh đã xoay vào file
        val file = File(
            requireContext().externalCacheDir,
            "captured_image_${System.currentTimeMillis()}.jpg"
        )
        FileOutputStream(file).use {
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        image.close()

        val savedUri = Uri.fromFile(file)
        GlobalScope.launch(Dispatchers.Main) {
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                "url",
                savedUri.path
            )
            findNavController().navigateUp()
        }
    }

    private fun getJpegOrientation(): Int {
        val rotation = requireActivity().windowManager.defaultDisplay.rotation
        val orientations = SparseIntArray()
        orientations.append(Surface.ROTATION_0, 90)
        orientations.append(Surface.ROTATION_90, 0)
        orientations.append(Surface.ROTATION_180, 270)
        orientations.append(Surface.ROTATION_270, 180)

        val sensorOrientation = requireContext().getSystemService(Context.CAMERA_SERVICE)
            .let { it as CameraManager }
            .getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0

        return (orientations[rotation] + sensorOrientation + 270) % 360
    }


    private fun closeCamera() {
        if (isRecording) {
            stopRecording()
        }
        cameraCaptureSession.close()
        cameraDevice?.close()
        cameraDevice = null
        imageReader.close()
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").apply { start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        backgroundThread = null
        backgroundHandler = null
    }

    fun requestFragmentPermission(permissions: Array<String>?) {
        requestPermissionLauncher.launch(permissions)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val grantResults =
                permissions.entries.map { if (it.value) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED }
                    .toIntArray()
            handlePermissionResult(grantResults)
        }


    private fun handlePermissionResult(grantResults: IntArray) {
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            setUpCamera()
        } else {
            showCameraPermissionDialog()
        }
    }

    private fun showCameraPermissionDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("The app needs camera access permission to perform this function.")
            .setPositiveButton("OK") { _, _ ->
                navigateToAppSettings()
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                findNavController().navigateUp()
            }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun navigateToAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        permissionLauncher.launch(intent)
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            // Reset the flag when returning from app settings
            isPermissionRequested = false
            // Recheck permissions after returning from app settings
            checkCameraPermission()

        }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
    }
}

