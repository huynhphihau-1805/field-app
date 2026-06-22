package com.crayon.fieldapp.ui.screen.bank

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.crayon.fieldapp.AppDispatchers
import com.crayon.fieldapp.data.model.SimpleModel
import com.crayon.fieldapp.data.remote.request.CreateUserRequest
import com.crayon.fieldapp.data.remote.response.GetMessageResponse
import com.crayon.fieldapp.data.remote.response.UserResponse
import com.crayon.fieldapp.data.repository.UserRepository
import com.crayon.fieldapp.ui.base.BaseViewModel
import com.crayon.fieldapp.utils.CityUtils
import com.crayon.fieldapp.utils.Resource
import com.crayon.fieldapp.utils.Status
import com.example.moviedb.utils.toRequestBody
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BankViewModel(
    private val userRepository: UserRepository, private val context: Context,
    private val dispatchers: AppDispatchers
) :
    BaseViewModel() {
    var user = MutableLiveData<UserResponse>()
    var banks = MutableLiveData<ArrayList<SimpleModel>>()

    fun getBankInfo() {
        viewModelScope.launch {
            try {
                user.value = userRepository.getMyUser()
            } catch (e: Exception) {
                onLoadFail(e)
            }
        }
    }

    private val bank = MediatorLiveData<Resource<GetMessageResponse>>()
    val updateBank: LiveData<Resource<GetMessageResponse>> get() = bank
    private var bankSource: LiveData<Resource<GetMessageResponse>> = MutableLiveData()
    fun updateBankInfo(tax: String, bankNumber: String, bankName: String, bankBrand: String) =
        viewModelScope.launch(dispatchers.main) {
            val request = CreateUserRequest(
                tax = tax.toRequestBody(),
                bank_number = bankNumber.toRequestBody(),
                bank_name = bankName.toRequestBody(),
                bank_brand = bankBrand.toRequestBody()
            )
            bank.removeSource(bankSource)
            withContext(dispatchers.io) {
                bankSource = userRepository.updateUser(request)
            }
            bank.addSource(bankSource) {
                bank.value = it
                if (it.status == Status.ERROR) {
                    it.message?.let { error ->
                        viewModelScope?.launch {
                            onLoadFail(error)
                        }
                    }
                }
            }

        }

    fun getListBank() {
        viewModelScope.launch {
            try {
                var mBanks = ArrayList<SimpleModel>()
                CityUtils.getAllBank(context).forEach {
                    mBanks.add(SimpleModel(it.name, it.id))
                }
                banks.value = mBanks
            } catch (e: Exception) {
                onLoadFail(e)
            }
        }
    }

}