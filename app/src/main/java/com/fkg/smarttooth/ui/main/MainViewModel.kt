package com.fkg.smarttooth.ui.main

import androidx.lifecycle.asLiveData
import com.fkg.smarttooth.base.BaseViewModel
import com.fkg.smarttooth.data.firebase.FirebaseUtil
import com.fkg.smarttooth.data.model.Jaw
import com.fkg.smarttooth.data.model.Patient
import com.fkg.smarttooth.utils.Utils.twoDecimalPlaces
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.exp

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebase: FirebaseUtil
) : BaseViewModel() {

    fun getPatients() = firebase.getAllPatients().asLiveData()

    fun getPatient(id: String) = firebase.getPatient(id).asLiveData()

    fun addPatient(name: String, age: Int, gender: String) = wrapApiWithLiveData(
        apiCall = {
            val data = Patient("", name, gender, age, "-", "-")
            firebase.addPatient(data)
        }
    )

    fun editPatient(patient: Patient) = wrapApiWithLiveData(
        apiCall = { firebase.setPatient(patient) }
    )

    fun deletePatient(id: String) = wrapApiWithLiveData(
        apiCall = { firebase.deletePatient(id) }
    )

    fun verifyLogin() = firebase.isLoggedIn()

    fun filterList(oldList: List<Patient>, query: String) = oldList.filter {
        it.name.lowercase().contains(query.lowercase())
    }

    fun getJawsData(id: String) = wrapApiWithLiveData(
        apiCall = { firebase.getJaws(id) }
    )

    fun diagnoseDiscrepancy(archLength: Double, toothData: Map<Jaw.TOOTH, Double>): Pair<String, String> {
        val totalToothLength = toothData.entries.fold(0.0) { acc, entry ->
            acc + entry.value
        }
        val space = archLength - totalToothLength

        val diagnosis =
            if (space < 0 ) "berdesakan"
            else if (space > 0) "diastema"
            else "normal"

        val level =
            if (abs(space) < 4) "mild"
            else if (abs(space) < 8) "moderate"
            else "severe"

        val diagnosisExplanation =
            when (diagnosis) {
                "berdesakan" -> "Dikarenakan panjang lengkung rahang kurang dari total panjang gigi, maka hasil diskrepansi ruang adalah berdesakan"
                "diastema" -> "Dikarenakan panjang lengkung rahang lebih dari total panjang gigi, maka hasil diskrepansi ruang adalah diastema"
                "normal" -> "Dikarenakan panjang lengkung rahang sama dengan total panjang gigi, maka hasil diskrepansi ruang adalah normal"
                else -> "Data tidak valid"
            }

        val explanation =
            "Total panjang gigi : $totalToothLength \n" +
            "Panjang lengkung rahang : $archLength \n" +
            "Panjang lengkung rahang - total panjang gigi : $space \n\n" +
            diagnosisExplanation

        return Pair("$diagnosis ($level)", explanation)
    }

    fun diagnoseArchShape(length: Double, mpv: Double, mmv: Double): Pair<String, String> {
        val diagnosis = if (length > mpv && length > mmv)
            "tapered"
        else if (length == mpv || length == mmv)
            "ovoid"
        else if (length < mpv && length < mmv)
            "squared"
        else
            "not valid"

        val diagnosisExplanation =
            when (diagnosis) {
                "tapered" -> "Dikarenakan panjang lebih dari lebar 1 (mpv) dan lebar 2 (mmv), maka bentuk rahang adalah tapered"
                "ovoid" -> "Dikarenakan panjang sama dengan lebar 1 (mpv) atau lebar 2 (mmv), maka bentuk rahang adalah ovoid"
                "squared" -> "Dikarenakan panjang kurang dari lebar 1 (mpv) dan lebar 2 (mmv), maka bentuk rahang adalah squared"
                else -> "Data tidak valid"
            }

        val explanation =
            "Panjang : $length \n" +
            "Lebar 1 (mpv) : $mpv \n" +
            "Lebar 2 (mmv) : $mmv \n\n" +
            diagnosisExplanation

        return Pair(diagnosis, explanation)
    }

    fun diagnoseIndexPont(toothData: Map<Jaw.TOOTH, Double>, mpv: Double, mmv: Double): Pair<String, String> {
        val incisor = listOf(
            Jaw.TOOTH.INCISOR_1,
            Jaw.TOOTH.INCISOR_2,
            Jaw.TOOTH.INCISOR_3,
            Jaw.TOOTH.INCISOR_4
        )
        val sumOfIncisor = toothData.entries.fold(0.0) { acc, entry ->
            val addedVal = if (incisor.contains(entry.key)) entry.value else 0.0
            acc + addedVal
        }
        val cpv = sumOfIncisor * 100 / 85
        val cmv = sumOfIncisor * 100 / 64
        val premolar = mpv - cpv
        val molar = mmv - cmv

        val diagnosis =
            if (premolar >= 0 && molar >= 0) "Tidak membutuhkan ekspansi"
            else "Membutuhkan ekspansi"

        val diagnosisExplanation =
            when (diagnosis) {
                "Tidak membutuhkan ekspansi" -> "Dikarenakan MPV dan MMV lebih dari CPV dan CMV, maka tidak membutuhkan ekspansi"
                "Membutuhkan ekspansi" -> "Dikarenakan MPV atau MMV kurang dari CPV atau CMV, maka membutuhkan ekspansi"
                else -> "Data tidak valid"
            }

        val explanation =
            "Total panjang insisivus : $sumOfIncisor \n" +
            "Calculated premolar value (cpv) : ${cpv.twoDecimalPlaces()} \n" +
            "Calculated molar value (cmv) : ${cmv.twoDecimalPlaces()} \n" +
            "Measured premolar value (mpv) : $mpv \n" +
            "Measured molar value (mmv) : $mmv \n" +
            "mpv - cpv : ${premolar.twoDecimalPlaces()} \n" +
            "mmv - cmv : ${molar.twoDecimalPlaces()} \n\n" +
            diagnosisExplanation

        return Pair(diagnosis, explanation)
    }

    fun diagnoseBolton(upperToothData: Map<Jaw.TOOTH, Double>, lowerToothData: Map<Jaw.TOOTH, Double>): Pair<String, String> {
        val except = listOf(
            Jaw.TOOTH.MOLAR_1,
            Jaw.TOOTH.MOLAR_4
        )
        val sumUpper = upperToothData.entries.fold(0.0) { acc, entry ->
            val addedVal = if (except.contains(entry.key)) 0.0 else entry.value
            acc + addedVal
        }
        val sumLower = lowerToothData.entries.fold(0.0) { acc, entry ->
            val addedVal = if (except.contains(entry.key)) 0.0 else entry.value
            acc + addedVal
        }
        val ratio = sumLower / sumUpper * 100

        val diagnosis =
            if (ratio > 91.3) "ukuran gigi-gigi rahang bawah terlalu besar"
            else "ukuran gigi-gigi rahang atas terlalu besar"

        val diagnosisExplanation =
            if (ratio > 91.3) "Dikarenakan rasio lebih dari 91.3, maka ukuran gigi-gigi rahang bawah terlalu besar"
            else "Dikarenakan rasio kurang dari 91.3, maka ukuran gigi-gigi rahang atas terlalu besar"

        val explanation =
            "Total panjang mandibula : $sumLower \n" +
            "Total panjang maxilla : $sumUpper \n" +
            "rasio : ${ratio.twoDecimalPlaces()} \n\n" +
            diagnosisExplanation

        return Pair(diagnosis, explanation)
    }
}