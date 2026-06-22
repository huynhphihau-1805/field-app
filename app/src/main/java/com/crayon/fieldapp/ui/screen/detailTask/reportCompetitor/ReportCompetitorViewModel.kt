package com.crayon.fieldapp.ui.screen.detailTask.reportCompetitor

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.crayon.fieldapp.data.local.pref.PrefHelper
import com.crayon.fieldapp.data.remote.response.GetMessageResponse
import com.crayon.fieldapp.data.remote.response.GetReportOpponentListResponse
import com.crayon.fieldapp.data.remote.response.Store
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.data.repository.TaskRepository
import com.crayon.fieldapp.ui.base.BaseViewModel
import com.crayon.fieldapp.ui.screen.detailTask.base.DetailTaskViewModel
import com.crayon.fieldapp.utils.BitmapUtils
import com.crayon.fieldapp.utils.Event
import com.crayon.fieldapp.utils.Resource
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import io.nlopez.smartlocation.SmartLocation
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class ReportCompetitorViewModel(
    private val context: Context,
    val taskRepository: TaskRepository,
    private val pref: PrefHelper
) : BaseViewModel() {

    private val _createActivity = MediatorLiveData<Event<Resource<GetMessageResponse>>>()
    val createActivity: LiveData<Event<Resource<GetMessageResponse>>> get() = _createActivity
    fun createActivity(
        taskId: String,
        brandName: String,
        mReportType: String,
        note: String,
        listUri: ArrayList<String>
    ) = viewModelScope.launch {
        _createActivity.postValue(Event(Resource.loading(null)))
        try {
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

                    val result = taskRepository.uploadReportOpponents(
                        taskId = taskId,
                        brandName = brandName,
                        type = mReportType,
                        note = note,
                        file1 = fileToUpload1
                    )
                    _createActivity.postValue(Event(Resource.success(result.data)))
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

                    val result = taskRepository.uploadReportOpponents(
                        taskId = taskId,
                        brandName = brandName,
                        type = mReportType,
                        note = note,
                        file1 = fileToUpload1,
                        file2 = fileToUpload2
                    )
                    _createActivity.postValue(Event(Resource.success(result.data)))
                }

                else -> {
                    val type1 = getTypeMedia(listUri.get(0))
                    val requestBody1: RequestBody = RequestBody.create(type1, File(listUri.get(0)))

                    val type2 = getTypeMedia(listUri.get(1))
                    val requestBody2: RequestBody = RequestBody.create(type2, File(listUri.get(1)))

                    val type3 = getTypeMedia(listUri.get(2))
                    val requestBody3: RequestBody = RequestBody.create(type3, File(listUri.get(2)))

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
                    val result = taskRepository.uploadReportOpponents(
                        taskId = taskId,
                        brandName = brandName,
                        type = mReportType,
                        note = note,
                        file1 = fileToUpload1,
                        file2 = fileToUpload2,
                        file3 = fileToUpload3
                    )
                    _createActivity.postValue(Event(Resource.success(result.data)))
                }
            }
        } catch (e: Exception) {
            _createActivity.postValue(Event(Resource.error(Throwable(), null)))
            onLoadFail(e)
        }
    }

    suspend fun createFile(
        context: Context,
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

    private fun getTypeMedia(url: String): MediaType {
        if (url.contains("mp4")) {
            return MediaType.get("video/mp4")
        } else {
            return MediaType.get("image/jpeg")
        }
    }

    private val _reportOpponents =
        MediatorLiveData<Event<Resource<GetReportOpponentListResponse>>>()
    val reportOpponents: LiveData<Event<Resource<GetReportOpponentListResponse>>> get() = _reportOpponents
    fun getReportOpponents(taskId: String) {
        viewModelScope.launch {
            fetchCurrentLocation()
            _reportOpponents.postValue(Event(Resource.loading(null)))
            try {
                val result = taskRepository.getPicReportOpponents(taskId)
                _reportOpponents.postValue(Event(Resource.success(result.data)))
            } catch (e: Exception) {
                _reportOpponents.postValue(Event(Resource.error(Throwable(), null)))
                onLoadFail(e)
            }
        }
    }

    var currentLocation: LatLng = pref.getCurrentLocation()
    var storeLocation: LatLng? = null
    var strDistant = "0.0 Km"
    fun verifyLocation(store: Store): Boolean {
        storeLocation = LatLng(store.lat ?: 0.0, store.lng ?: 0.0)
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
}