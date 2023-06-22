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
    enum class TOOTH(val value: String, val display: String, @DrawableRes val imgUpper: Int, @DrawableRes val imgBottom: Int) : Parcelable {
        MOLAR_1("molar_1", "Molar 1", R.drawable.arch_26, R.drawable.arch_36),
        PREMOLAR_2("premolar_2", "Premolar 2", R.drawable.arch_25, R.drawable.arch_35),
        PREMOLAR_1("premolar_1", "Premolar 1", R.drawable.arch_24, R.drawable.arch_34),
        CANINE_1("canine_1", "Canine", R.drawable.arch_23, R.drawable.arch_33),
        INCISOR_2("incisor_2", "Incisor 2", R.drawable.arch_22, R.drawable.arch_32),
        INCISOR_1("incisor_1", "Incisor 1", R.drawable.arch_21, R.drawable.arch_31),
        INCISOR_3("incisor_3", "Incisor 1", R.drawable.arch_11, R.drawable.arch_41),
        INCISOR_4("incisor_4", "Incisor 2", R.drawable.arch_12, R.drawable.arch_42),
        CANINE_2("canine_2", "Canine", R.drawable.arch_13, R.drawable.arch_43),
        PREMOLAR_3("premolar_3", "Premolar 1", R.drawable.arch_14, R.drawable.arch_44),
        PREMOLAR_4("premolar_4", "Premolar 2", R.drawable.arch_15, R.drawable.arch_45),
        MOLAR_3("molar_3", "Molar 1", R.drawable.arch_16, R.drawable.arch_46)
    }

    companion object {
        fun DocumentSnapshot.toJaw(): Jaw? = try {
            val toothDataMap = get("toothData") as? Map<String, Double>?
            val toothData = toothDataMap!!.mapKeys { TOOTH.valueOf(it.key.uppercase()) }
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
