package com.fkg.smarttooth.data.model

import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.util.Log
import com.fkg.smarttooth.R
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

data class Jaw(
    val id: String,
    val toothData: Map<TOOTH, Double>,
    val archLength: Double,
    val length: Double,
    val mpv: Double,
    val mmv: Double
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "toothData" to toothData.mapKeys { it.key.value },
            "archLength" to archLength,
            "length" to length,
            "mpv" to mpv,
            "mmv" to mmv
        )
    }

    @Parcelize
    enum class TOOTH(val value: String, @DrawableRes val img: Int) : Parcelable {
        MOLAR_1("Molar 1", R.drawable.molar_1),
        MOLAR_2("Molar 2", R.drawable.molar_2),
        PREMOLAR_1("Premolar 1", R.drawable.premolar_1),
        PREMOLAR_2("Premolar 2", R.drawable.premolar_2),
        CANINE_1("Canine 1" , R.drawable.canine_1),
        INCISOR_1("Incisor 1", R.drawable.incisor_1),
        INCISOR_2("Incisor 2", R.drawable.incisor_2),
        INCISOR_3("Incisor 3", R.drawable.incisor_3),
        INCISOR_4("Incisor 4", R.drawable.incisor_4),
        CANINE_2("Canine 2", R.drawable.canine_2),
        PREMOLAR_3("Premolar 3", R.drawable.premolar_3),
        PREMOLAR_4("Premolar 4", R.drawable.premolar_4),
        MOLAR_3("Molar 3", R.drawable.molar_3),
        MOLAR_4("Molar 4", R.drawable.molar_4),
    }

    companion object {
        fun DocumentSnapshot.toJaw(): Jaw? = try {
            val toothDataMap = get("toothData") as? Map<String, Double>?
            val toothData = toothDataMap!!.mapKeys { TOOTH.valueOf(it.key.uppercase().replace(" ", "_")) }
            val archLength = getDouble("archLength")!!
            val length = getDouble("length")!!
            val mpv = getDouble("mpv")!!
            val mmv = getDouble("mmv")!!
            Jaw(id, toothData, archLength, length, mpv, mmv)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting jaw data", e)
            FirebaseCrashlytics.getInstance().log("Error converting jaw data")
            FirebaseCrashlytics.getInstance().setCustomKey("jawId", id)
            FirebaseCrashlytics.getInstance().recordException(e)
            null
        }

        private const val TAG = "Jaw"
    }
}
