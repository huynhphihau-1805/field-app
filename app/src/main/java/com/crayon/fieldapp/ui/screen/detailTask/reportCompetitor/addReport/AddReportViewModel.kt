package com.crayon.fieldapp.ui.screen.detailTask.reportCompetitor.addReport

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.crayon.fieldapp.data.remote.response.GetMessageResponse
import com.crayon.fieldapp.data.remote.response.GetReportOpponentListResponse
import com.crayon.fieldapp.data.remote.response.TaskResponse
import com.crayon.fieldapp.data.repository.TaskRepository
import com.crayon.fieldapp.ui.base.BaseViewModel
import com.crayon.fieldapp.utils.BitmapUtils
import com.crayon.fieldapp.utils.Event
import com.crayon.fieldapp.utils.Resource
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class AddReportViewModel(val taskRepository: TaskRepository) : BaseViewModel() {

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
                    val requestBody1: RequestBody = RequestBody.create(type1,  File(listUri.get(0)))

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
}