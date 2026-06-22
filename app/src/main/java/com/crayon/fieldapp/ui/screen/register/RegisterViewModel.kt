package com.crayon.fieldapp.ui.screen.register

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.crayon.fieldapp.AppDispatchers
import com.crayon.fieldapp.data.remote.request.CreateUserRequest
import com.crayon.fieldapp.data.remote.request.UpdateProfileForm
import com.crayon.fieldapp.data.remote.response.GetMessageResponse
import com.crayon.fieldapp.data.repository.UserRepository
import com.crayon.fieldapp.ui.base.BaseViewModel
import com.crayon.fieldapp.utils.BitmapUtils
import com.crayon.fieldapp.utils.Resource
import com.crayon.fieldapp.utils.Status
import com.example.moviedb.utils.toRequestBody
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RegisterViewModel(
    private val userRepository: UserRepository,
    private val dispatchers: AppDispatchers,
    private val context: Context
) : BaseViewModel() {

    private val _avatarRef = MutableLiveData<String>()
    val avatarRefData: LiveData<String> get() = _avatarRef
    private val _bodyRef = MutableLiveData<String>()
    val bodyRefData: LiveData<String> get() = _bodyRef
    private val _idFrontRef = MutableLiveData<String>()
    val idFrontRefData: LiveData<String> get() = _idFrontRef
    private val _idBackRef = MutableLiveData<String>()
    val idBackRefData :LiveData<String> get() = _idBackRef
    private val _firstName = MutableLiveData<String>()
    val firstName :LiveData<String> get() = _firstName
    private val _lastname = MutableLiveData<String>()
    val lastname :LiveData<String> get() = _lastname
    private val _birthday = MutableLiveData<String>()
    val birthday :LiveData<String> get() = _birthday
    private val _email = MutableLiveData<String>()
    val email :LiveData<String> get() = _email
    private val _password = MutableLiveData<String>()
    val password :LiveData<String> get() = _password
    private val _confirmPassword = MutableLiveData<String>()
    val confirmPassword :LiveData<String> get() = _confirmPassword
    private val _phone = MutableLiveData<String>()
    val phone :LiveData<String> get() = _phone
    private val _gender = MutableLiveData<String>()
    val gender :LiveData<String> get() = _gender
    private val _tax = MutableLiveData<String>()
    val tax :LiveData<String> get() = _tax
    private val _bankNumber = MutableLiveData<String>()
    val bankNumber :LiveData<String> get() = _bankNumber
    private val _bankName = MutableLiveData<Int>()
    val bankName :LiveData<Int> get() = _bankName
    private val _bankBrand = MutableLiveData<String>()
    val bankBrand :LiveData<String> get() = _bankBrand
    private val _heigth = MutableLiveData<String>()
    val heigth :LiveData<String> get() = _heigth
    private val _weigth = MutableLiveData<String>()
    val weigth :LiveData<String> get() = _weigth
    private val _id = MutableLiveData<String>()
    val id :LiveData<String> get() = _id
    private val _city = MutableLiveData<Int>()
    val city :LiveData<Int> get() = _city
    private val _temporaryCity = MutableLiveData<Int>()
    val temporaryCity :LiveData<Int> get() = _temporaryCity
    private val _district = MutableLiveData<Int>()
    val district :LiveData<Int> get() = _district
    private val _temporaryDistrict = MutableLiveData<Int>()
    val temporaryDistrict :LiveData<Int> get() = _temporaryDistrict
    private val _temporaryAddress = MutableLiveData<String>()
    val temporaryAddress :LiveData<String> get() = _temporaryAddress

    fun setAvatarRef(uri: String) {
        _avatarRef.value = uri
    }

    fun setFirstName(data: String) {
        _firstName.value = data
    }

    fun setLastName(data: String) {
        _lastname.value = data
    }

    fun setBirthDay(data: String) {
        _birthday.value = data
    }

    fun setEmail(data: String) {
        _email.value = data
    }

    fun setPassword(data: String) {
        _password.value = data
    }

    fun setConfirmPassword(data: String) {
        _confirmPassword.value = data
    }

    fun setPhone(data: String) {
        _phone.value = data
    }

    fun setGender(data: String) {
        _gender.value = data
    }

    fun setTax(data: String) {
        _tax.value = data
    }

    fun setBankNumber(data: String) {
        _bankNumber.value = data
    }

    fun setBankName(data: Int) {
        _bankName.value = data
    }

    fun setBankBrand(data: String) {
        _bankBrand.value = data
    }

    fun setHeight(data: String) {
        _heigth.value = data
    }

    fun setWeigth(data: String) {
        _weigth.value = data
    }

    fun setId(data: String) {
        _id.value = data
    }

    fun setCity(data: Int) {
        _city.value = data
    }

    fun setTemporaryCity(data: Int) {
        _temporaryCity.value = data
    }

    fun setDistrict(data: Int) {
        _district.value = data
    }

    fun setTemporaryDistrict(data: Int) {
        _temporaryDistrict.value = data
    }

    fun setBodyRef(uri: String) {
        _bodyRef.value = uri
    }

    fun setIdFrontRef(uri: String) {
        _idFrontRef.value = uri
    }

    fun setIdBackRef(uri: String) {
        _idBackRef.value = uri
    }


    private val _register = MediatorLiveData<Resource<GetMessageResponse>>()
    val result: LiveData<Resource<GetMessageResponse>> get() = _register
    private var userSource: LiveData<Resource<GetMessageResponse>> = MutableLiveData()
    fun register(form: UpdateProfileForm) = viewModelScope.launch(dispatchers.main) {
        val avatarFile = File(form.avatar!!)
        val bodyFile = File(form.full_body!!)
        val idBack = File(form.id_back!!)
        val idFront = File(form.id_front!!)


        val requestBodyAvatar: RequestBody = RequestBody.create(MediaType.get("multipart/form-data"), avatarFile)
        val avatarFileUpload: MultipartBody.Part =
            MultipartBody.Part.createFormData("avatar", avatarFile.getName(), requestBodyAvatar)

        val requestBodyFullBody: RequestBody = RequestBody.create(MediaType.get("multipart/form-data"), bodyFile)
        val bodyFileUpload: MultipartBody.Part =
            MultipartBody.Part.createFormData("full_body_image", bodyFile.getName(), requestBodyFullBody)

        val requestBodyFront: RequestBody = RequestBody.create(MediaType.get("multipart/form-data"), idFront)
        val idFrontFileUpload: MultipartBody.Part =
            MultipartBody.Part.createFormData(
                "identification_image_front",
                idFront!!.getName(),
                requestBodyFront
            )

        val requestBodyBack: RequestBody = RequestBody.create(MediaType.get("multipart/form-data"), idBack)
        val idBackFileUpload: MultipartBody.Part =
            MultipartBody.Part.createFormData(
                "identification_image_back",
                idBack!!.getName(),
                requestBodyBack
            )


        val request = CreateUserRequest(
            firstName = form.firstName!!.toRequestBody(),
            lastName = form.lastName!!.toRequestBody(),
            birthDay = form.birthDay!!.toRequestBody(),
            email = form.email!!.toRequestBody(),
            phone = form.phone!!.toRequestBody(),
            gender = form.gender!!.toRequestBody(),
            tax = form.tax!!.toRequestBody(),
            bank_number = form.bank_number!!.toRequestBody(),
            bank_name = form.bank_name!!.toRequestBody(),
            bank_brand = form.bank_brand!!.toRequestBody(),
            heigth = form.heigth!!.toRequestBody(),
            weigth = form.weigth!!.toRequestBody(),
            id = form.id!!.toRequestBody(),
            city = form.city!!.toRequestBody(),
            district = form.district!!.toRequestBody(),
            address = form.address!!.toRequestBody(),
            temporary_address = form.temporary_address!!.toRequestBody(),
            temporary_district = form.temporary_district!!.toRequestBody(),
            temporary_city = form.temporary_city!!.toRequestBody(),
            password = form.password!!.toRequestBody(),
            confirmPassword = form.confirmPassword!!.toRequestBody(),
            avatar = avatarFileUpload,
            full_body = bodyFileUpload,
            id_back = idBackFileUpload,
            id_front = idFrontFileUpload
        )
        _register.removeSource(userSource)
        withContext(dispatchers.io) {
            userSource = userRepository.register(request)
        }
        _register.addSource(userSource) {
            _register.value = it
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
//            mView.showDialogMessage("Can not found Image")
            return null
        }
        // reduce image size
        var bitmap = BitmapUtils.decodeFile(originPath, 0)

        if (bitmap == null) {
//            mView.showDialogMessage("Can not found Image")
            return null
        }

        // get time
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = type + "_" + timeStamp

        // create the temp file to upload
        val childFile: File?
        childFile = BitmapUtils.createImageFromBitmap(context, bitmap, 50, imageFileName)
        if (childFile == null) {
//            mView.showDialogMessage("Can not found Image")
//            mView.hideProgressDialog()
            return null
        }
        // release bitmap
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