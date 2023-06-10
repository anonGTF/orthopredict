package com.fkg.smarttooth.ui.auth

import androidx.lifecycle.viewModelScope
import com.fkg.smarttooth.base.BaseViewModel
import com.fkg.smarttooth.data.firebase.FirebaseUtil
import com.fkg.smarttooth.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebase: FirebaseUtil
) : BaseViewModel() {

    fun login(email: String, password: String) = wrapApiWithLiveData(
        apiCall = { firebase.login(email, password) }
    )

    fun register(name: String, institution: String, email: String, password: String) =
        wrapApiWithLiveData(
            apiCall = { firebase.register(name, institution, email, password) }
        )
}