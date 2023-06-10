package com.fkg.smarttooth.ui.main

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import com.fkg.smarttooth.R
import com.fkg.smarttooth.base.BaseActivity
import com.fkg.smarttooth.data.model.Patient
import com.fkg.smarttooth.databinding.ActivityAddPatientBinding
import com.fkg.smarttooth.utils.Utils.safeParseDouble
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPatientActivity : BaseActivity<ActivityAddPatientBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityAddPatientBinding =
        ActivityAddPatientBinding::inflate

    private var data: Patient? = null
    private val viewModel: MainViewModel by viewModels()

    override fun setup() {
        setTitle("Tambah Pasien")
        setupBackButton()
        data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras?.getParcelable(EXTRA_DATA, Patient::class.java)
        } else {
            intent.extras?.getParcelable(EXTRA_DATA)
        }

        if (data != null) {
            setupEditMode()
        } else {
            setupAddMode()
        }

        setupDropdown()
    }

    private fun setupDropdown() {
        val genderAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this, R.layout.item_gender, GENDER
        )
        binding.inputGender.setAdapter(genderAdapter)
    }

    private fun setupAddMode() {
        with(binding) {
            btnAdd.setOnClickListener {
                val name = etName.text.toString()
                val age = etAge.text.toString().toInt()
                val gender = inputGender.text.toString()
                viewModel.addPatient(name, age, gender)
                .observe(this@AddPatientActivity, setAddObserver()) }
        }
    }

    private fun setAddObserver() = setObserver<Patient?>(
        onSuccess = {
            binding.progressBar.gone()
            goToActivity(DetailPatientActivity::class.java, Bundle().apply {
                putString(DetailPatientActivity.EXTRA_ID, it.data?.id)
            }, isFinish = true)
        },
        onError = {
            binding.progressBar.gone()
            showToast(it.message.toString())
        },
        onLoading = { binding.progressBar.visible() }
    )

    private fun setupEditMode() {
        with(binding) {
            etName.setText(data?.name)
            etAge.setText(data?.age.toString())
            inputGender.setText(data?.gender)
            btnAdd.text = "Update Data Pasien"

            btnAdd.setOnClickListener {
                viewModel.editPatient(getUpdatedData())
                    .observe(this@AddPatientActivity, setEditObserver())
            }
        }
    }

    private fun setEditObserver() = setObserver<Void?>(
        onSuccess = {
            binding.progressBar.gone()
            goToActivity(DetailPatientActivity::class.java, Bundle().apply {
                putString(DetailPatientActivity.EXTRA_ID, data?.id)
            }, isFinish = true)
        },
        onError = {
            binding.progressBar.gone()
            setActivityResult(RESULT_CANCELED, Uri.EMPTY)
        },
        onLoading = { binding.progressBar.visible() }
    )

    private fun getUpdatedData() = Patient(
        data?.id.orEmpty(),
        binding.etName.text.toString(),
        binding.inputGender.text.toString(),
        binding.etAge.text.toString().safeParseDouble().toInt(),
        data?.discrepancyResult.orEmpty(),
        data?.archShapeResult.orEmpty()
    )

    companion object {
        const val EXTRA_DATA = "extra_data"
        val GENDER = listOf("Laki-laki", "Perempuan")
    }
}