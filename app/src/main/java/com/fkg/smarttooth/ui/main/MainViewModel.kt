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
        val except = listOf(
            Jaw.TOOTH.MOLAR_1,
            Jaw.TOOTH.MOLAR_3
        )
        val totalToothLength = toothData.entries.fold(0.0) { acc, entry ->
            val addedVal = if (except.contains(entry.key)) 0.0 else entry.value
            acc + addedVal
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
            "Total panjang gigi : ${totalToothLength.twoDecimalPlaces()} \n" +
            "Panjang lengkung rahang : ${archLength.twoDecimalPlaces()} \n" +
            "Panjang lengkung rahang - total panjang gigi : ${space.twoDecimalPlaces()} \n\n" +
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
                "tapered" -> "Dikarenakan panjang lebih dari lebar 1 (anterior) dan lebar 2 (posterior), maka bentuk rahang adalah tapered"
                "ovoid" -> "Dikarenakan panjang sama dengan lebar 1 (anterior) atau lebar 2 (posterior), maka bentuk rahang adalah ovoid"
                "squared" -> "Dikarenakan panjang kurang dari lebar 1 (anterior) dan lebar 2 (posterior), maka bentuk rahang adalah squared"
                else -> "Data tidak valid"
            }

        val explanation =
            "Panjang : ${length.twoDecimalPlaces()} \n" +
            "Lebar 1 (anterior) : ${mpv.twoDecimalPlaces()} \n" +
            "Lebar 2 (posterior) : ${mmv.twoDecimalPlaces()} \n\n" +
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
            "Total panjang insisivus : ${sumOfIncisor.twoDecimalPlaces()} \n" +
            "Calculated premolar value (cpv) : ${cpv.twoDecimalPlaces()} \n" +
            "Calculated molar value (cmv) : ${cmv.twoDecimalPlaces()} \n" +
            "Measured premolar value (mpv) : ${mpv.twoDecimalPlaces()} \n" +
            "Measured molar value (mmv) : ${mmv.twoDecimalPlaces()} \n" +
            "mpv - cpv : ${premolar.twoDecimalPlaces()} \n" +
            "mmv - cmv : ${molar.twoDecimalPlaces()} \n\n" +
            diagnosisExplanation

        return Pair(diagnosis, explanation)
    }

    fun diagnoseOverallBolton(upperToothData: Map<Jaw.TOOTH, Double>, lowerToothData: Map<Jaw.TOOTH, Double>): Pair<String, String> {
        val sumUpper = upperToothData.entries.fold(0.0) { acc, entry -> acc + entry.value }
        val sumLower = lowerToothData.entries.fold(0.0) { acc, entry -> acc + entry.value }
        val ratio = sumLower / sumUpper * 100

        val diagnosis =
            if (ratio > 91.3) "ukuran gigi-gigi rahang bawah terlalu besar"
            else "ukuran gigi-gigi rahang atas terlalu besar"

        val diagnosisExplanation =
            if (ratio > 91.3) "Dikarenakan rasio lebih dari 91.3, maka ukuran gigi-gigi rahang bawah terlalu besar"
            else "Dikarenakan rasio kurang dari 91.3, maka ukuran gigi-gigi rahang atas terlalu besar"

        val explanation =
            "Total panjang mandibula : ${sumLower.twoDecimalPlaces()} \n" +
                    "Total panjang maxilla : ${sumUpper.twoDecimalPlaces()} \n" +
                    "rasio : ${ratio.twoDecimalPlaces()} \n\n" +
                    diagnosisExplanation

        return Pair(diagnosis, explanation)
    }

    fun diagnoseAnteriorBolton(upperToothData: Map<Jaw.TOOTH, Double>, lowerToothData: Map<Jaw.TOOTH, Double>): Pair<String, String> {
        val mesiodistal = listOf(
            Jaw.TOOTH.CANINE_1,
            Jaw.TOOTH.INCISOR_1,
            Jaw.TOOTH.INCISOR_2,
            Jaw.TOOTH.INCISOR_3,
            Jaw.TOOTH.INCISOR_4,
            Jaw.TOOTH.CANINE_2,
        )
        val sumUpper = upperToothData.entries.fold(0.0) { acc, entry ->
            val addedVal = if (mesiodistal.contains(entry.key)) entry.value else 0.0
            acc + addedVal
        }
        val sumLower = lowerToothData.entries.fold(0.0) { acc, entry ->
            val addedVal = if (mesiodistal.contains(entry.key)) entry.value else 0.0
            acc + addedVal
        }
        val ratio = sumLower / sumUpper * 100

        val diagnosis =
            if (ratio > 77.2) "ukuran gigi-gigi rahang bawah terlalu besar"
            else "ukuran gigi-gigi rahang atas terlalu besar"

        val diagnosisExplanation =
            if (ratio > 77.2) "Dikarenakan rasio anterior lebih dari 77.2, maka ukuran gigi-gigi rahang bawah terlalu besar"
            else "Dikarenakan rasio anterior kurang dari 77.2, maka ukuran gigi-gigi rahang atas terlalu besar"

        val explanation =
            "Total panjang 6 gigi mandibula : ${sumLower.twoDecimalPlaces()} \n" +
                    "Total panjang 6 gigi maxilla : ${sumUpper.twoDecimalPlaces()} \n" +
                    "rasio anterior : ${ratio.twoDecimalPlaces()} \n\n" +
                    diagnosisExplanation

        return Pair(diagnosis, explanation)
    }
}