package com.fkg.smarttooth.data.model

import android.os.Parcelable
import android.util.Log
import com.fkg.smarttooth.base.BaseModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    override val id: String,
    val name: String,
    val institution: String
) : BaseModel(id), Parcelable {
    companion object {
        fun DocumentSnapshot.toUser(): User? = try {
            val name = getString("name")!!
            val institution = getString("institution")!!
            User(id, name, institution)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting user", e)
            FirebaseCrashlytics.getInstance().log("Error converting user")
            FirebaseCrashlytics.getInstance().setCustomKey("userId", id)
            FirebaseCrashlytics.getInstance().recordException(e)
            null
        }

        private const val TAG = "User"
    }
}
