package com.crayon.fieldapp.ui.screen.attendance

import android.content.Context
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
import com.crayon.fieldapp.ui.screen.detailTask.base.DetailTaskViewModel
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

class AttendanceViewModel(
    private val taskRepository: TaskRepository,
    private val dispatchers: AppDispatchers,
    private val context: Context,
    private val pref: PrefHelper
) : BaseViewModel() {
    // FOR DATA
    private val _users = MediatorLiveData<Resource<List<TaskResponse>>>()
    val listTask: LiveData<Resource<List<TaskResponse>>> get() = _users
    private var usersSource: LiveData<Resource<List<TaskResponse>>> = MutableLiveData()

    fun getTodayPicAttendances() =
        viewModelScope.launch(dispatchers.main) {
            _users.removeSource(usersSource)
            withContext(dispatchers.io) {
                usersSource = taskRepository.getTodayPicAttendances()
            }
            _users.addSource(usersSource) {
                _users.value = it
                if (it.status == Status.ERROR) {
                    it.message?.let { error ->
                        viewModelScope.launch {
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
        if (distant > DetailTaskViewModel.MAX_VALID_DISTANT) {
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

    private val _updateTask = MediatorLiveData<Resource<GetMessageResponse>>()
    val updateTask: LiveData<Resource<GetMessageResponse>> get() = _updateTask
    private var updateTaskSource: LiveData<Resource<GetMessageResponse>> = MutableLiveData()
    fun upLoadTask(task: TaskResponse, imageUrl: String) =
        viewModelScope.launch(dispatchers.main) {
            val mediaType = getTypeMedia(imageUrl)
            val requestBody1: RequestBody = RequestBody.create(mediaType, File(imageUrl))

            val fileToUpload1: MultipartBody.Part =
                MultipartBody.Part.createFormData(
                    "files",
                    File(imageUrl).getName(),
                    requestBody1
                )
            _updateTask.removeSource(updateTaskSource)
            withContext(dispatchers.io) {
                val attendanceStatus = TaskUtils.getStatusAttendances(task)
                when (attendanceStatus) {
                    AttendanceStatus.PENDING.value -> {
                        updateTaskSource = taskRepository.checkIn(
                            taskId = task.id.toString(),
                            file = fileToUpload1
                        )
                    }

                    AttendanceStatus.PROCESSING.value -> {
                        updateTaskSource = taskRepository.checkOut(
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

    private fun getTypeMedia(url: String): MediaType {
        if (url.contains("mp4")) {
            return MediaType.get("video/mp4")
        } else {
            return MediaType.get("image/jpeg")
        }
    }

}