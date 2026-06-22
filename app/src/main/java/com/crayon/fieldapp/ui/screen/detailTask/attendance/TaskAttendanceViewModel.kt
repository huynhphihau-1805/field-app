package com.crayon.fieldapp.ui.screen.detailTask.attendance

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.crayon.fieldapp.AppDispatchers
import com.crayon.fieldapp.data.local.pref.PrefHelper
import com.crayon.fieldapp.data.remote.response.AttendanceStatus
import com.crayon.fieldapp.data.remote.response.GetMessageResponse
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.data.repository.TaskRepository
import com.crayon.fieldapp.ui.base.BaseViewModel
import com.crayon.fieldapp.utils.BitmapUtils
import com.crayon.fieldapp.utils.Event
import com.crayon.fieldapp.utils.Resource
import com.crayon.fieldapp.utils.Status
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import io.nlopez.smartlocation.SmartLocation
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import studio.phillip.yolo.utils.TaskUtils
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date


class TaskAttendanceViewModel(
    private val taskRepository: TaskRepository,
    private val dispatchers: AppDispatchers,
    private val context: Context,
    private val pref: PrefHelper
) : BaseViewModel() {

    private val _task = MediatorLiveData<Event<Resource<TaskResponse>>>()
    val task: LiveData<Event<Resource<TaskResponse>>> get() = _task

    private val _isEnableCheckIn = MediatorLiveData<Boolean>(true)
    val isEnableCheckIn: LiveData<Boolean>  get() = _isEnableCheckIn

    private val _isEnableCheckOut = MediatorLiveData<Boolean>(true)
    val isEnableCheckOut: LiveData<Boolean>  get() = _isEnableCheckOut

    private val _subtitle = MediatorLiveData<String>()
    val subtitle: LiveData<String>  get() = _subtitle

    fun getDetailTask(taskId: String) {
        viewModelScope.launch {
            _task.postValue(Event(Resource.loading(null)))
            try {
                fetchCurrentLocation()
                val result = taskRepository.getPicTask(taskId)
                _task.postValue(Event(Resource.success(result.data)))
                result.data?.let {
                    it.attendances?.let { attendances ->
                        if (attendances.size == 0) {
                            _subtitle.postValue("Check In")
                            _isEnableCheckIn.postValue(true)
                            _isEnableCheckOut.postValue(false)
                        } else if (attendances.size == 1 && attendances.get(0).checkOutTime == null) {
                            _subtitle.postValue("Check Out")
                            _isEnableCheckIn.postValue(false)
                            _isEnableCheckOut.postValue(true)
                        } else {
                            _subtitle.postValue("Đã chấm công")
                            _isEnableCheckIn.postValue(false)
                            _isEnableCheckOut.postValue(false)
                        }
                    }
                }

            } catch (e: Exception) {
                onLoadFail(e)
            }
        }
    }

    private val _updateTask = MediatorLiveData<Resource<GetMessageResponse>>()
    val updateTask: LiveData<Resource<GetMessageResponse>> get() = _updateTask
    private var updateTaskSource: LiveData<Resource<GetMessageResponse>> = MutableLiveData()
    fun upLoadTask(task: TaskResponse, listUri: ArrayList<String>, note: String? = null) =
        viewModelScope.launch(dispatchers.main) {
            when (listUri.size) {
                1 -> {
                    val type = getTypeMedia(listUri.get(0))
                    val requestBody1: RequestBody = RequestBody.create(type, File(listUri.get(0)))

                    val fileToUpload1: MultipartBody.Part =
                        MultipartBody.Part.createFormData(
                            "files",
                            File(listUri.get(0)).getName(),
                            requestBody1
                        )
                    _updateTask.removeSource(updateTaskSource)
                    withContext(dispatchers.io) {
                        updateTaskSource = taskRepository.uploadImage(
                            taskId = task.id.toString(),
                            notes = note,
                            file1 = fileToUpload1
                        )
                    }
                    _updateTask.addSource(updateTaskSource) {
                        _updateTask.value = it
                        if (it.status == Status.ERROR) {
                            it.message?.let { error ->
                                viewModelScope?.launch {
                                    onLoadFail(error)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    val type1 = getTypeMedia(listUri.get(0))
                    val requestBody1: RequestBody = RequestBody.create(type1, File(listUri.get(0)))

                    val type2 = getTypeMedia(listUri.get(1))
                    val requestBody2: RequestBody = RequestBody.create(type2, File(listUri.get(1)))

                    val fileToUpload1: MultipartBody.Part =
                        MultipartBody.Part.createFormData(
                            "files",
                            File(listUri.get(0)).getName(),
                            requestBody1
                        )
                    val fileToUpload2: MultipartBody.Part =
                        MultipartBody.Part.createFormData(
                            "files",
                            File(listUri.get(1)).getName(),
                            requestBody2
                        )
                    _updateTask.removeSource(updateTaskSource)
                    withContext(dispatchers.io) {
                        updateTaskSource = taskRepository.uploadImage(
                            taskId = task.id.toString(),
                            notes = note,
                            file1 = fileToUpload1,
                            file2 = fileToUpload2
                        )
                    }
                    _updateTask.addSource(updateTaskSource) {
                        _updateTask.value = it
                        if (it.status == Status.ERROR) {
                            it.message?.let { error ->
                                viewModelScope?.launch {
                                    onLoadFail(error)
                                }
                            }
                        }
                    }
                }

                else -> {

                    val type1 = getTypeMedia(listUri.get(0))
                    val requestBody1: RequestBody = RequestBody.create(type1, File(listUri.get(0)))

                    val type2 = getTypeMedia(listUri.get(1))
                    val requestBody2: RequestBody = RequestBody.create(type1, File(listUri.get(1)))

                    val type3 = getTypeMedia(listUri.get(2))
                    val requestBody3: RequestBody = RequestBody.create(type1, File(listUri.get(2)))

                    val fileToUpload1: MultipartBody.Part =
                        MultipartBody.Part.createFormData(
                            "files",
                            File(listUri.get(0)).getName(),
                            requestBody1
                        )
                    val fileToUpload2: MultipartBody.Part =
                        MultipartBody.Part.createFormData(
                            "files",
                            File(listUri.get(1)).getName(),
                            requestBody2
                        )
                    val fileToUpload3: MultipartBody.Part =
                        MultipartBody.Part.createFormData(
                            "files",
                            File(listUri.get(2)).getName(),
                            requestBody3
                        )
                    _updateTask.removeSource(updateTaskSource)
                    withContext(dispatchers.io) {
                        updateTaskSource = taskRepository.uploadImage(
                            taskId = task.id.toString(),
                            notes = note,
                            file1 = fileToUpload1,
                            file2 = fileToUpload2,
                            file3 = fileToUpload3
                        )
                    }
                    _updateTask.addSource(updateTaskSource) {
                        _updateTask.value = it
                        if (it.status == Status.ERROR) {
                            it.message?.let { error ->
                                viewModelScope?.launch {
                                    onLoadFail(error)
                                }
                            }
                        }
                    }
                }
            }
        }

    private val _updateCheckInOutTask = MediatorLiveData<Resource<GetMessageResponse>>()
    val updateCheckInOutTask: LiveData<Resource<GetMessageResponse>> get() = _updateCheckInOutTask
    private var updateCheckInOutTaskSource: LiveData<Resource<GetMessageResponse>> =
        MutableLiveData()

    fun checkInOut(task: TaskResponse, listUri: ArrayList<String>, note: String? = null) =
        viewModelScope.launch(dispatchers.main) {
            val type = getTypeMedia(listUri.get(0))
            val requestBody1: RequestBody = RequestBody.create(type, File(listUri.get(0)))

            val fileToUpload1: MultipartBody.Part =
                MultipartBody.Part.createFormData(
                    "files",
                    File(listUri.get(0)).getName(),
                    requestBody1
                )
            _updateCheckInOutTask.removeSource(updateCheckInOutTaskSource)
            withContext(dispatchers.io) {
                val attendanceStatus = TaskUtils.getStatusAttendances(task)
                when (attendanceStatus) {
                    AttendanceStatus.PENDING.value -> {
                        updateCheckInOutTaskSource = taskRepository.checkIn(
                            taskId = task.id.toString(),
                            file = fileToUpload1
                        )
                    }

                    AttendanceStatus.PROCESSING.value -> {
                        updateCheckInOutTaskSource = taskRepository.checkOut(
                            taskId = task.id.toString(),
                            file = fileToUpload1
                        )
                    }

                    AttendanceStatus.COMPLETED.value -> {
                        showError(Throwable("Bạn đã chấm công rồi"))
                        return@withContext
                    }
                }

            }
            _updateCheckInOutTask.addSource(updateCheckInOutTaskSource) {
                _updateCheckInOutTask.value = it
                if (it.status == Status.ERROR) {
                    it.message?.let { error ->
                        viewModelScope?.launch {
                            onLoadFail(error)
                        }
                    }
                }
            }
        }

    var currentLocation: LatLng = pref.getCurrentLocation()
    var storeLocation: LatLng? = null
    var strDistant = "0.0 Km"
    fun verifyLocation(task: TaskResponse): Boolean {
        storeLocation = LatLng(task.store!!.lat, task.store!!.lng)
        val distant = SphericalUtil.computeDistanceBetween(currentLocation, storeLocation)
        if (distant > MAX_VALID_DISTANT) {
            if (distant > 1000) {
                strDistant = Math.round(distant / 1000).toString() + "Km"
            } else {
                strDistant = Math.round(distant).toString() + "m"
            }
            return false
        } else {
            return true
        }
    }

    fun fetchCurrentLocation() {
        SmartLocation.with(context).location()
            .oneFix()
            .start {
                pref.setCurrentLocation(LatLng(it.latitude, it.longitude))
                currentLocation = LatLng(it.latitude, it.longitude)
            }
    }

    companion object {
        const val MAX_VALID_DISTANT = 300.0 // 300m
    }

    private fun getTypeMedia(url: String): MediaType {
        if (url.contains("mp4")) {
            return MediaType.get("video/mp4")
        } else {
            return MediaType.get("image/jpeg")
        }
    }

    suspend fun createFile(
        url: String,
        task: TaskResponse,
        quality: Int,
        isHasTage: Boolean
    ): File? {
        if (!url.contains("mp4")) {
            return BitmapUtils.createImageFileToUpload(
                context,
                url,
                task,
                quality,
                isHasTage
            )
        } else {
            return File(url)
        }
    }

    fun uriToFile(context: Context, uri: Uri): File? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.let {
            val outputFile = File(context.cacheDir, "videoFile.mp4")
            val outputStream: OutputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (it.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            it.close()
            outputStream.close()
            return outputFile
        }
        return null
    }


    private val _removeImage = MediatorLiveData<Resource<GetMessageResponse>>()
    val isRemoveTask: LiveData<Resource<GetMessageResponse>> get() = _removeImage
    private var removeImageSource: LiveData<Resource<GetMessageResponse>> = MutableLiveData()
    fun removeImage(taskId: String, ids: ArrayList<String>) =
        viewModelScope.launch(dispatchers.main) {
            _removeImage.removeSource(removeImageSource)
            withContext(dispatchers.io) {
                removeImageSource = taskRepository.deleteAttachment(taskId, ids)
            }
            _removeImage.addSource(removeImageSource) {
                _removeImage.value = it
                if (it.status == Status.ERROR) {
                    it.message?.let { error ->
                        viewModelScope?.launch {
                            onLoadFail(error)
                        }
                    }
                }
            }
        }

    fun createImageFileToUpload(
        context: Context,
        originPath: String,
        type: String
    ): File? {
        if (TextUtils.isEmpty(originPath)) {
            return null
        }
        // reduce image size
        var bitmap = BitmapUtils.decodeFile(originPath, 200)

        if (bitmap == null) {
            return null
        }

        // get time
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = type + "_" + timeStamp

        // create the temp file to upload
        val childFile: File?
        childFile = BitmapUtils.createImageFromBitmap(context, bitmap, 100, imageFileName)
        if (childFile == null) {
            return null
        }
        bitmap = null
        return childFile
    }

    fun bitmapToFile(bitmap: Bitmap, fileName: String, context: Context): File {
        val file = File(context.cacheDir, fileName)
        file.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        return file
    }

}