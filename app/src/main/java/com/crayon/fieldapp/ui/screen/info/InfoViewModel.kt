package com.crayon.fieldapp.ui.screen.info

import android.content.Context
import android.net.Uri
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
import com.crayon.fieldapp.ui.screen.info.model.UpdateInfoForm
import com.crayon.fieldapp.utils.Resource
import com.crayon.fieldapp.utils.Status
import com.example.moviedb.utils.toRequestBody
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class InfoViewModel(
    private val userRepository: UserRepository,
    private val context: Context,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {

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
    val uploadImage: LiveData<Resource<GetMessageResponse>> get() = _avatar
    private var avatarSource: LiveData<Resource<GetMessageResponse>> = MutableLiveData()
    fun updateIdFront(avatar: Uri) = viewModelScope.launch(dispatchers.main) {
        val avatarFile = File(avatar.path)

        val type = MediaType.get("multipart/form-data")
        val requestBody: RequestBody = RequestBody.create(type, avatarFile)
        val avatarFileUpload: MultipartBody.Part =
            MultipartBody.Part.createFormData(
                "identification_image_front",
                avatarFile.getName(),
                requestBody
            )
        val request = CreateUserRequest(
            id_front = avatarFileUpload
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

    fun updateIdBack(fullBody: Uri) = viewModelScope.launch(dispatchers.main) {
        val fullBodyFile = File(fullBody.path)

        val type = MediaType.get("multipart/form-data")
        val requestBody: RequestBody = RequestBody.create(type, fullBodyFile)
        val fullBodyFileUpload: MultipartBody.Part =
            MultipartBody.Part.createFormData(
                "identification_image_back",
                fullBodyFile.getName(),
                requestBody
            )
        val request = CreateUserRequest(
            id_back = fullBodyFileUpload
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

    private val _info = MediatorLiveData<Resource<GetMessageResponse>>()
    val updateInfo: LiveData<Resource<GetMessageResponse>> get() = _info
    private var infoSource: LiveData<Resource<GetMessageResponse>> = MutableLiveData()
    fun updateInfo(form: UpdateInfoForm) =
        viewModelScope.launch(dispatchers.main) {
            _info.removeSource(infoSource)
            withContext(dispatchers.io) {
                val request = CreateUserRequest(
                    firstName = form.firstName!!.toRequestBody(),
                    lastName = form.lastName!!.toRequestBody(),
                    birthDay = form.birthDay!!.toRequestBody(),
                    email = form.email!!.toRequestBody(),
                    phone = form.phone!!.toRequestBody(),
                    gender = form.gender!!.toRequestBody(),
                    heigth = form.heigth!!.toRequestBody(),
                    weigth = form.weigth!!.toRequestBody(),
                    id = form.id!!.toRequestBody(),
                    city = form.city!!.toRequestBody(),
                    district = form.district!!.toRequestBody(),
                    address = form.address!!.toRequestBody(),
                    temporary_address = form.current_address!!.toRequestBody(),
                    temporary_city = form.current_city!!.toRequestBody(),
                    temporary_district = form.current_district!!.toRequestBody()
                )

                infoSource = userRepository.updateUser(request)
            }
            _info.addSource(infoSource) {
                _info.value = it
                if (it.status == Status.ERROR) {
                    it.message?.let { error ->
                        viewModelScope.launch {
                            onLoadFail(error)
                        }
                    }
                }
            }
        }
}