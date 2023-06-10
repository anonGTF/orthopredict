package com.fkg.smarttooth.ui.profile

import com.fkg.smarttooth.base.BaseViewModel
import com.fkg.smarttooth.data.firebase.FirebaseUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebase: FirebaseUtil
) : BaseViewModel() {

    fun getUser() = wrapApiWithLiveData(
        apiCall = { firebase.getDetailUser() }
    )

    fun logout() = wrapApiWithLiveData(
        apiCall = { firebase.logout() }
    )

}