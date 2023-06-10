package com.fkg.smarttooth.data.model

import android.os.Parcelable
import android.util.Log
import com.fkg.smarttooth.base.BaseModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

@Parcelize
data class Patient(
    override val id: String,
    val name: String,
    val gender: String,
    val age: Int,
    val discrepancyResult: String,
    val archShapeResult: String
) : BaseModel(id), Parcelable {
    companion object {
        fun DocumentSnapshot.toPatient(): Patient? =
            try {
                val name = getString("name")!!
                val gender = getString("gender")!!
                val discrepancyResult = getString("discrepancyResult")!!
                val archShapeResult = getString("archShapeResult")!!
                val age = getDouble("age")!!.toInt()
                Patient(id, name, gender, age, discrepancyResult, archShapeResult)
            } catch (e: Exception) {
                Log.e(TAG, "Error converting patient", e)
                FirebaseCrashlytics.getInstance().log("Error converting patient")
                FirebaseCrashlytics.getInstance().setCustomKey("patientId", id)
                FirebaseCrashlytics.getInstance().recordException(e)
                null
            }

        private const val TAG = "Patient"
    }
}
