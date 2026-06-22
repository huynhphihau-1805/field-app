package com.crayon.fieldapp.ui.screen.detailGroupUser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.crayon.fieldapp.AppDispatchers
import com.crayon.fieldapp.data.remote.response.MemberResponse
import com.crayon.fieldapp.data.repository.GroupUserRepository
import com.crayon.fieldapp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class DetailGroupUserViewModel(
    private val groupUserRepository: GroupUserRepository,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {
    val members = MutableLiveData<List<MemberResponse>>()


    fun getMemberOfGroupUser(agencyId: String, groupUserId: String) {
        viewModelScope.launch {
            try {
                members.value = groupUserRepository.getMemberOfGroup(agencyId, groupUserId).data ?: emptyList()

            } catch (e: Exception) {
                onLoadFail(e)
            }
        }
    }
}