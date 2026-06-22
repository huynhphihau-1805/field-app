package com.crayon.fieldapp.ui.screen.avatar

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.crayon.fieldapp.AppDispatchers
import com.crayon.fieldapp.data.remote.request.CreateUserRequest
import com.crayon.fieldapp.data.remote.response.GetMessageResponse
import com.crayon.fieldapp.data.remote.response.UserResponse
import com.crayon.fieldapp.data.repository.UserRepository
import com.crayon.fieldapp.ui.base.BaseViewModel
import com.crayon.fieldapp.utils.BitmapUtils
import com.crayon.fieldapp.utils.Resource
import com.crayon.fieldapp.utils.Status
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class AvatarViewModel(
    private val userRepository: UserRepository, private val dispatchers: AppDispatchers,
    private val context: Context
) : BaseViewModel() {
    var user = MutableLiveData<UserResponse>()

    fun getUserInfo() {
        viewModelScope.launch {
            try {
                user.value = userRepository.getMyUser()
            } catch (e: Exception) {
                onLoadFail(e)
            }
        }
    }

    private val _avatar = MediatorLiveData<Resource<GetMessageResponse>>()
    val avatar: LiveData<Resource<GetMessageResponse>> get() = _avatar
    private var avatarSource: LiveData<Resource<GetMessageResponse>> = MutableLiveData()
    fun updateAvatar(avatar: Uri) = viewModelScope.launch(dispatchers.main) {
        val avatarFile = File(avatar.path)
        val mediaType = MediaType.get("image/jpeg")
        val requestBody: RequestBody = RequestBody.create(mediaType, avatarFile)
        val avatarFileUpload: MultipartBody.Part =
            MultipartBody.Part.createFormData("avatar", avatarFile.getName(), requestBody)
        val request = CreateUserRequest(
            avatar = avatarFileUpload
        )
        _avatar.removeSource(avatarSource)
        withContext(dispatchers.io) {
            avatarSource = userRepository.updateUser(request)
        }
        _avatar.addSource(avatarSource) {
            _avatar.value = it
            if (it.status == Status.ERROR) {
                it.message?.let { error ->
                    viewModelScope?.launch {
                        onLoadFail(error)
                    }
                }
            }
        }
    }

    fun updateFullBody(fullBody: Uri) = viewModelScope.launch(dispatchers.main) {
        val fullBodyFile = File(fullBody.path)
        val mediaType = MediaType.get("image/jpeg")
        val requestBody: RequestBody = RequestBody.create(mediaType, fullBodyFile)
        val fullBodyFileUpload: MultipartBody.Part =
            MultipartBody.Part.createFormData(
                "full_body_image",
                fullBodyFile.getName(),
                requestBody
            )
        val request = CreateUserRequest(
            full_body = fullBodyFileUpload
        )
        _avatar.removeSource(avatarSource)
        withContext(dispatchers.io) {
            avatarSource = userRepository.updateUser(request)
        }
        _avatar.addSource(avatarSource) {
            _avatar.value = it
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
        childFile = BitmapUtils.createImageFromBitmap(context, bitmap, 50, imageFileName)
        if (childFile == null) {
            return null
        }
        bitmap = null
        return childFile
    }

    fun createFile(
        url: String,
        quality: Int,
    ): File? {
        if (!url.contains("mp4")) {
            return BitmapUtils.createImageFileToUpload(
                context,
                url,
                quality
            )
        } else {
            return File(url)
        }
    }
}