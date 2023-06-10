package com.fkg.smarttooth.ui.inputdata

import com.fkg.smarttooth.base.BaseViewModel
import com.fkg.smarttooth.data.firebase.FirebaseUtil
import com.fkg.smarttooth.data.model.Jaw
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.EnumMap
import javax.inject.Inject

@HiltViewModel
class InputDataViewModel @Inject constructor(
    private val firebase: FirebaseUtil
): BaseViewModel() {
    private val toothData: MutableMap<Jaw.TOOTH, Double> = EnumMap(Jaw.TOOTH::class.java)

    fun getJawData(id: String, isUpperJaw: Boolean) = wrapApiWithLiveData(
        apiCall = { firebase.getJaw(id, isUpperJaw) }
    )

    fun setJawData(id: String, isUpperJaw: Boolean, archLength: Double,
                   length: Double, mpv: Double, mmv: Double) = wrapApiWithLiveData(
        apiCall = {
            val data = Jaw(id, toothData, archLength, length, mpv, mmv)
            firebase.setJaw(id, isUpperJaw, data)
        }
    )

    fun verifyLogin() = firebase.isLoggedIn()

    fun put(key: Jaw.TOOTH, value: Double) {
        if (toothData.containsKey(key)) {
            toothData[key] = value
        } else {
            toothData[key] = value
        }
    }
}