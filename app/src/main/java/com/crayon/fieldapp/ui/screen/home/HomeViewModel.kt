package com.crayon.fieldapp.ui.screen.home

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.crayon.fieldapp.AppDispatchers
import com.crayon.fieldapp.data.local.dao.ProductDao
import com.crayon.fieldapp.data.remote.ApiService
import com.crayon.fieldapp.data.remote.response.JobResponse
import com.crayon.fieldapp.data.remote.response.RoleOfAgency
import com.crayon.fieldapp.data.remote.response.RoleResponse
import com.crayon.fieldapp.data.repository.JobRepository
import com.crayon.fieldapp.data.repository.RoleRepository
import com.crayon.fieldapp.ui.base.BaseViewModel
import com.crayon.fieldapp.utils.Event
import com.crayon.fieldapp.utils.Resource
import com.crayon.fieldapp.utils.SingleLiveEvent
import com.crayon.fieldapp.utils.Status
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class HomeViewModel(
    private val jobRepository: JobRepository,
    private val roleRepository: RoleRepository,
    private val productDao: ProductDao,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {
    // FOR DATA
    private val _jobs = MediatorLiveData<Event<Resource<List<JobResponse>>>>()
    val jobs: LiveData<Event<Resource<List<JobResponse>>>> get() = _jobs
    private val _isShowMonitorTab = MediatorLiveData<Boolean>()
    val isShowMonitorTab: LiveData<Boolean> get() = _isShowMonitorTab


    fun setIsShowMonitorTab(isShow: Boolean){
        _isShowMonitorTab.value = isShow
    }

    fun getTodayJob(statusTime: String, endTime: String, skip: Int) = viewModelScope.launch(dispatchers.main) {
        viewModelScope.launch {
            _jobs.postValue(Event(Resource.loading(null)))
            try {
                val result = jobRepository.getTodayPicJob(statusTime, endTime, skip = skip)
                _jobs.postValue(Event(Resource.success(result.data)))
            } catch (e: Exception) {
                onLoadFail(e)
            }
        }
    }

    fun deleteProduct(){
        viewModelScope.launch {
            try {
                productDao.deleteProduct(Date().time)
            } catch (e: Exception) {
                onLoadFail(e)
            }
        }
    }

    private val _listAgency = MediatorLiveData<Event<Resource<List<RoleResponse>>>>()
    val listAgency: LiveData<Event<Resource<List<RoleResponse>>>> get() = _listAgency
    private var listAgencySource: LiveData<Resource<List<RoleResponse>>> = MutableLiveData()
    fun getListRole() = viewModelScope.launch(dispatchers.main) {
        _listAgency.removeSource(listAgencySource)
        withContext(dispatchers.io) {
            listAgencySource = roleRepository.getListRole()
        }
        _listAgency.addSource(listAgencySource) {
            _listAgency.value = Event(it)
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