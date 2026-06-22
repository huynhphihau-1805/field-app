package com.crayon.fieldapp.ui.base

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crayon.fieldapp.data.local.pref.PrefHelper
import com.crayon.fieldapp.data.remote.convertToBaseException
import com.crayon.fieldapp.data.repository.UserRepository
import com.crayon.fieldapp.utils.SingleLiveEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class BaseViewModel : ViewModel(), KoinComponent {
    // loading flag
    val isLoading = MutableLiveData<Boolean>().apply { value = false }
    val auth by inject<UserRepository>()
    val appPrefs by inject<PrefHelper>()

    // error message
    val errorMessage = SingleLiveEvent<String>()

    // optional flags
    val noInternetConnectionEvent = SingleLiveEvent<Boolean>()
    val tokenExpiredEvent = SingleLiveEvent<Boolean>()
    val connectTimeoutEvent = SingleLiveEvent<Boolean>()

    // exception handler for coroutine
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        viewModelScope.launch {
            onLoadFail(throwable)
        }
    }

    /**
     * handle throwable when load fail
     */
    open suspend fun onLoadFail(throwable: Throwable) {
        withContext(Dispatchers.Main) {
            when (throwable) {
                // case no internet connection
                is UnknownHostException -> {
                    noInternetConnectionEvent.value = true
                }
                // case request time out
                is SocketTimeoutException -> {
                    connectTimeoutEvent.value = true
                }

                else -> {
                    // convert throwable to base exception to get error information
                    val baseException = convertToBaseException(throwable)
                    when (baseException.httpCode) {
                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            refreshToken(appPrefs.getRefreshToken().toString())
                        }

                        HttpURLConnection.HTTP_INTERNAL_ERROR, HttpURLConnection.HTTP_BAD_REQUEST -> {
                            errorMessage.value = baseException.serverErrorResponse?.message ?: "Unknown error occurred"
                        }

                        else -> {
                            val invalidParam = baseException.serverErrorResponse?.validations
                            if (invalidParam != null) {
                                errorMessage.value = invalidParam.get(0).message ?: "Unknown error occurred"
                            } else {
                                val errorTmpMessage = baseException.serverErrorResponse?.message
                                if (errorTmpMessage.isNullOrEmpty()) {
                                    errorMessage.value = "Vui lòng thử lại"
                                } else {
                                    errorMessage.value = errorTmpMessage ?: "Unknown error occurred"
                                }

                            }
                        }
                    }
                }
            }
            hideLoading()
        }
    }

    fun refreshToken(refreshToken: String) {
        viewModelScope.launch {
            try {
                val res = auth.refreshToken(refreshToken)
                appPrefs.setToken("Bearer " + res.token.toString())
                appPrefs.setRefreshToken(res.refresh_token.toString())
            } catch (e: Exception) {
                tokenExpiredEvent.value = true
            }
        }
    }

    open fun showError(e: Throwable) {
        errorMessage.value = e.message
    }

    fun showLoading() {
        isLoading.value = true
    }

    fun hideLoading() {
        isLoading.value = false
    }

    override fun onCleared() {
        super.onCleared()
    }
}