package com.crayon.fieldapp.ui.screen.camera

import androidx.fragment.app.Fragment

class CameraXFragment : Fragment() {

//    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
//
//    private lateinit var imagePreview: Preview
//
//    private lateinit var imageAnalysis: ImageAnalysis
//    private var imageCapture: ImageCapture? = null
//    private var videoCapture: VideoCapture<Recorder>? = null
//    private var recording: Recording? = null
//    private lateinit var cameraExecutor: ExecutorService
//
//    private lateinit var outputDirectory: File
//
//    private lateinit var cameraControl: CameraControl
//
//    private lateinit var cameraInfo: CameraInfo
//
//    private var linearZoom = 0f
//
//    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
//
//    private var isRecording = false
//
//    private var isTakeImage: Boolean = false
//
//    private var timer: CountDownTimer? = null
//
//    private lateinit var binding: FragmentCameraxBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        isTakeImage = requireArguments().getBoolean("isTakeImage", false)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentCameraxBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    @SuppressLint("RestrictedApi")
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        if (isTakeImage) {
//            binding.tvTime.visibility = View.GONE
//        } else {
//            binding.tvTime.visibility = View.VISIBLE
//            timer = object : CountDownTimer(30000, 1000) {
//                override fun onTick(millisUntilFinished: Long) {
//                    binding.tvTime.setText("Thời gian clip còn:" + "00:" + millisUntilFinished / 1000)
//                }
//
//                @SuppressLint("RestrictedApi")
//                override fun onFinish() {
//                    videoCapture.stopRecording()
//                }
//            }
//        }
//
//        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//
//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            requestPermissions()
//        }
//
//        outputDirectory = getOutputDirectory(requireContext())
//
//        if (isTakeImage) {
//            binding.cameraCaptureButton.setSingleClick {
//                takePicture()
//            }
//        } else {
//            binding.cameraCaptureButton.setOnClickListener {
//                if (isRecording) {
//                    binding.icSwitchCamera.visibility = View.VISIBLE
//                    timer?.cancel()
//                    videoCapture.stopRecording()
//                    it.isSelected = false
//                    isRecording = false
//                } else {
//                    timer?.start()
//                    recordVideo()
//                    it.isSelected = true
//                    isRecording = true
//                    binding.icSwitchCamera.visibility = View.GONE
//                }
//            }
//        }
//
//        binding.icSwitchCamera.setSingleClick {
//            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
//                CameraSelector.LENS_FACING_BACK
//            } else {
//                CameraSelector.LENS_FACING_FRONT
//            }
//            startCamera()
//        }
//
//        cameraExecutor = Executors.newSingleThreadExecutor()
//    }
//
//    private fun requestPermissions() {
//        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
//    }
//
//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(
//            requireContext(), it) == PackageManager.PERMISSION_GRANTED
//    }
//
//
//    @SuppressLint("RestrictedApi")
//    private fun recordVideo() {
//        val file = createFile(
//            outputDirectory,
//            FILENAME,
//            VIDEO_EXTENSION
//        )
//        videoCapture.startRecording(
//            file,
//            ContextCompat.getMainExecutor(requireContext()),
//            object : VideoCapture.OnVideoSavedCallback {
//                override fun onVideoSaved(file: File) {
//                    val savedUri = Uri.fromFile(file)
//                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
//                        "url",
//                        savedUri.path
//                    )
//                    findNavController().navigateUp()
//
//                }
//
//                override fun onError(
//                    videoCaptureError: Int,
//                    message: String,
//                    cause: Throwable?
//                ) {
//                    val msg = "Video capture failed: $message"
//                    binding.previewView.post {
//                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
//                    }
//                }
//            })
//    }
//
//    private fun takePicture() {
//        val file = createFile(
//            outputDirectory,
//            FILENAME,
//            PHOTO_EXTENSION
//        )
//        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
//        imageCapture.takePicture(
//            outputFileOptions,
//            ContextCompat.getMainExecutor(requireContext()),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                    val savedUri = Uri.fromFile(file)
//                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
//                        "url",
//                        savedUri.path
//                    )
//                    findNavController().navigateUp()
//                }
//
//                override fun onError(exception: ImageCaptureException) {
//                    val msg = "Photo capture failed: ${exception.message}"
//                    binding.previewView.post {
//                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
//                    }
//                }
//            })
//    }
//
//    @SuppressLint("RestrictedApi")
//    private fun startCamera() {
//        imagePreview = Preview.Builder().apply {
//            setTargetAspectRatio(AspectRatio.RATIO_16_9)
//            setTargetRotation(binding.previewView.display.rotation)
//        }.build()
//
//        imageAnalysis = ImageAnalysis.Builder().apply {
//            setImageQueueDepth(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//        }.build()
//        imageAnalysis.setAnalyzer(
//            ContextCompat.getMainExecutor(requireContext()),
//            LuminosityAnalyzer()
//        )
//
//        imageCapture = ImageCapture.Builder().apply {
//            setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//            setFlashMode(ImageCapture.FLASH_MODE_AUTO)
//        }.build()
//
//        videoCapture = VideoCaptureConfig.Builder().apply {
//            setTargetAspectRatio(AspectRatio.RATIO_16_9)
//        }.build()
//
//        val orientationEventListener = object : OrientationEventListener(requireContext()) {
//            override fun onOrientationChanged(orientation: Int) {
//                // Monitors orientation values to determine the target rotation value
//                val rotation: Int = when (orientation) {
//                    in 45..134 -> Surface.ROTATION_270
//                    in 135..224 -> Surface.ROTATION_180
//                    in 225..314 -> Surface.ROTATION_90
//                    else -> Surface.ROTATION_0
//                }
//                imageCapture.targetRotation = rotation
//                videoCapture.setTargetRotation(rotation)
//            }
//        }
//        orientationEventListener.enable()
//
//
//        val cameraSelector =
//            CameraSelector.Builder().requireLensFacing(lensFacing).build()
//        cameraProviderFuture.addListener(Runnable {
//            val cameraProvider = cameraProviderFuture.get()
//
//
//            val camera = cameraProvider.bindToLifecycle(
//                viewLifecycleOwner,
//                cameraSelector,
//                imagePreview,
//                // imageAnalysis,
//                if (isTakeImage) imageCapture else videoCapture
//            )
//            binding.previewView.preferredImplementationMode =
//                PreviewView.ImplementationMode.TEXTURE_VIEW
//            imagePreview.setSurfaceProvider(preview_view.createSurfaceProvider(camera.cameraInfo))
//
//            cameraControl = camera.cameraControl
//            cameraInfo = camera.cameraInfo
//        }, ContextCompat.getMainExecutor(requireContext()))
//    }
//
//
//    private val activityResultLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestMultiplePermissions())
//        { permissions ->
//            var permissionGranted = true
//            permissions.entries.forEach {
//                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
//                    permissionGranted = false
//            }
//            if (!permissionGranted) {
//                Toast.makeText(requireContext(),
//                    "Permission request denied",
//                    Toast.LENGTH_SHORT).show()
//                findNavController().navigateUp()
//            } else {
//                startCamera()
//            }
//        }
//
//    private class LuminosityAnalyzer : ImageAnalysis.Analyzer {
//        private var lastAnalyzedTimestamp = 0L
//
//        /**
//         * Helper extension function used to extract a byte array from an
//         * image plane buffer
//         */
//        private fun ByteBuffer.toByteArray(): ByteArray {
//            rewind()    // Rewind the buffer to zero
//            val data = ByteArray(remaining())
//            get(data)   // Copy the buffer into a byte array
//            return data // Return the byte array
//        }
//
//        override fun analyze(image: ImageProxy) {
//            val currentTimestamp = System.currentTimeMillis()
//            // Calculate the average luma no more often than every second
//            if (currentTimestamp - lastAnalyzedTimestamp >=
//                TimeUnit.SECONDS.toMillis(1)
//            ) {
//                // Since format in ImageAnalysis is YUV, image.planes[0]
//                // contains the Y (luminance) plane
//                val buffer = image.planes[0].buffer
//                // Extract image data from callback object
//                val data = buffer.toByteArray()
//                // Convert the data into an array of pixel values
//                val pixels = data.map { it.toInt() and 0xFF }
//                // Compute average luminance for the image
//                val luma = pixels.average()
//                // Log the new luma value
//                Log.d("CameraXApp", "Average luminosity: $luma")
//                // Update timestamp of last analyzed frame
//                lastAnalyzedTimestamp = currentTimestamp
//            }
//            image.close()
//        }
//    }
//
//    companion object {
//        private const val TAG = "MainActivity"
//        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
//        private const val PHOTO_EXTENSION = ".jpg"
//        private const val VIDEO_EXTENSION = ".mp4"
//
//        private const val REQUEST_CODE_PERMISSIONS = 10
//
//        private val REQUIRED_PERMISSIONS =
//            mutableListOf (
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO
//            ).apply {
//                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
//                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                }
//            }.toTypedArray()
//
//        fun getOutputDirectory(context: Context): File {
//            val appContext = context.applicationContext
//            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
//                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
//            }
//            return if (mediaDir != null && mediaDir.exists())
//                mediaDir else appContext.filesDir
//        }
//
//        fun createFile(baseFolder: File, format: String, extension: String) =
//            File(
//                baseFolder, SimpleDateFormat(format, Locale.US)
//                    .format(System.currentTimeMillis()) + extension
//            )
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        cameraExecutor.shutdown()
//    }

}