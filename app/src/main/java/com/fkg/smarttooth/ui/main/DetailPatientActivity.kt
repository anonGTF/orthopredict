package com.fkg.smarttooth.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fkg.smarttooth.R
import com.fkg.smarttooth.base.BaseActivity
import com.fkg.smarttooth.data.model.Diagnosis
import com.fkg.smarttooth.data.model.DiagnosisDetail
import com.fkg.smarttooth.data.model.Jaw
import com.fkg.smarttooth.data.model.Patient
import com.fkg.smarttooth.databinding.ActivityDetailPatientBinding
import com.fkg.smarttooth.ui.inputdata.InputDataActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailPatientActivity : BaseActivity<ActivityDetailPatientBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityDetailPatientBinding =
        ActivityDetailPatientBinding::inflate

    private val viewModel: MainViewModel by viewModels()

    override fun setup() {
        setTitle("Detail Pasien")
        setupBackButton()
        val id = intent.extras?.getString(EXTRA_ID)
        id?.let {
            viewModel.getPatient(it).observe(this, setPatientObserver())
            viewModel.getJawsData(it).observe(this, setJawObserver())
        }
        if (!viewModel.verifyLogin()) {
            binding.btnEdit.gone()
            binding.btnDelete.gone()
        }
    }

    override fun onResume() {
        super.onResume()
        setup()
    }

    private fun setPatientObserver() = setObserver<Patient>(
        onSuccess = { response ->
            binding.progressBar.gone()
            response.data?.let { populatePatientData(it) }
        },
        onError = {
            binding.progressBar.gone()
            showToast(it.message.toString())
        },
        onLoading = { binding.progressBar.visible() }
    )

    private fun populatePatientData(patient: Patient) {
        with(binding) {
            tvName.text = patient.name
            tvAgeGender.text = "${patient.gender} - ${patient.age} Tahun"

            btnDelete.setOnClickListener { handleDelete(patient.id) }

            btnEdit.setOnClickListener {
                goToActivity(AddPatientActivity::class.java,
                    Bundle().apply { putParcelable(AddPatientActivity.EXTRA_DATA, patient) })
            }

            cardToothData.setOnClickListener {
                goToActivity(InputDataActivity::class.java, Bundle().apply {
                    putString(InputDataActivity.EXTRA_ID, patient.id)
                })
            }
        }
    }

    private fun handleDelete(id: String) {
        showConfirmationDialog(
            "Hapus Pasien",
            "Anda yakin ingin menghapus pasien ini?",
            onPositiveClicked = {
                viewModel.deletePatient(id).observe(this, setDeleteObserver())
            }
        )
    }

    private fun setDeleteObserver() = setObserver<Void?>(
        onSuccess = {
            binding.progressBar.gone()
            showToast("Pasien berhasil dihapus")
            goToActivity(MainActivity::class.java, isFinish = true)
        },
        onError = {
            binding.progressBar.gone()
            showToast(it.message.toString())
        },
        onLoading = { binding.progressBar.visible() }
    )

    private fun setJawObserver() = setObserver<Pair<Jaw?, Jaw?>?>(
        onSuccess = { response ->
            binding.progressBar.gone()
            response.data?.let {
                populateJawData(it.first, it.second)
            }
        },
        onError = {
            binding.progressBar.gone()
            showToast(it.message.toString())
        },
        onLoading = { binding.progressBar.visible() }
    )

    private fun populateJawData(mandible: Jaw?, maxilla: Jaw?) {
        val diagnosisList = listOf(
            getArchShapeDiagnosis(mandible, maxilla),
            getDiscrepancyDiagnosis(mandible, maxilla),
            getPontIndexDiagnosis(mandible, maxilla),
            getBoltonDiagnosis(mandible, maxilla),
        )

        val diagnosisAdapter = DiagnosisAdapter(diagnosisList)
        with(binding.rvResult) {
            adapter = diagnosisAdapter
            layoutManager = LinearLayoutManager(this@DetailPatientActivity)
        }
    }

    private fun getArchShapeDiagnosis(mandible: Jaw?, maxilla: Jaw?): Diagnosis {
        val (mbArchShape, mbExplanation) = safeResult(mandible, "Tidak ada data rahang bawah") {
            viewModel.diagnoseArchShape(it.length, it.mpv, it.mmv)
        }
        val (maArchShape, maExplanation) = safeResult(maxilla, "Tidak ada data rahang atas") {
            viewModel.diagnoseArchShape(it.length, it.mpv, it.mmv)
        }
        return Diagnosis(
            "Bentuk Rahang",
            listOf(
                DiagnosisDetail("Rahang Bawah", mbArchShape, mbExplanation),
                DiagnosisDetail("Rahang Atas", maArchShape, maExplanation)
            ),
            R.drawable.ic_arch_shape
        )
    }

    private fun getDiscrepancyDiagnosis(mandible: Jaw?, maxilla: Jaw?): Diagnosis {
        val (mbDiscrepancy, mbExplanation) = safeResult(mandible, "Tidak ada data rahang bawah") {
            viewModel.diagnoseDiscrepancy(it.archLength, it.toothData)
        }
        val (maDiscrepancy, maExplanation) = safeResult(maxilla, "Tidak ada data rahang atas") {
            viewModel.diagnoseDiscrepancy(it.archLength, it.toothData)
        }
        return Diagnosis(
            "Diskrepansi Ruang",
            listOf(
                DiagnosisDetail("Rahang Bawah", mbDiscrepancy, mbExplanation),
                DiagnosisDetail("Rahang Atas", maDiscrepancy, maExplanation)
            ),
            R.drawable.ic_discrepancy
        )
    }

    private fun getPontIndexDiagnosis(mandible: Jaw?, maxilla: Jaw?): Diagnosis {
        val (mbPont, mbExplanation) = safeResult(mandible, "Tidak ada data rahang bawah") {
            viewModel.diagnoseIndexPont(it.toothData, it.mpv, it.mmv)
        }
        val (maPont, maExplanation) = safeResult(maxilla, "Tidak ada data rahang atas") {
            viewModel.diagnoseIndexPont(it.toothData, it.mpv, it.mmv)
        }
        return Diagnosis(
            "Indeks Pont",
            listOf(
                DiagnosisDetail("Rahang Bawah", mbPont, mbExplanation),
                DiagnosisDetail("Rahang Atas", maPont, maExplanation)
            ),
            R.drawable.ic_pont
        )
    }

    private fun getBoltonDiagnosis(mandible: Jaw?, maxilla: Jaw?): Diagnosis {
        val (bolton, explanation) = if (mandible != null && maxilla != null)
            viewModel.diagnoseBolton(maxilla.toothData, mandible.toothData)
        else
            Pair("Data rahang atas dan rahang bawah harus diisi", "-")

        return Diagnosis(
            "Analisis Bolton",
            listOf(
                DiagnosisDetail("", bolton, explanation)
            ),
            R.drawable.ic_bolton
        )
    }

    private fun safeResult(jaw: Jaw?, nullValue: String, onNotNull : (Jaw) -> Pair<String, String>): Pair<String, String> =
        if (jaw != null)
            onNotNull.invoke(jaw)
        else
            Pair(nullValue, "-")


    companion object {
        const val EXTRA_ID = "extra_id"
    }
}