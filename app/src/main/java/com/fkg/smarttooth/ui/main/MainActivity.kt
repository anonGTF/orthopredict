package com.fkg.smarttooth.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.vvalidator.util.onTextChanged
import com.fkg.smarttooth.R
import com.fkg.smarttooth.base.BaseActivity
import com.fkg.smarttooth.data.model.Patient
import com.fkg.smarttooth.databinding.ActivityMainBinding
import com.fkg.smarttooth.ui.auth.LoginActivity
import com.fkg.smarttooth.ui.profile.ProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding =
        ActivityMainBinding::inflate

    private val patientAdapter: PatientAdapter by lazy { PatientAdapter() }
    private val viewModel: MainViewModel by viewModels()
    private var isLoggedIn = false
    private var allPatient: List<Patient> = listOf()
    private var job: Job? = null

    override fun setup() {
        setupRecyclerView()
        viewModel.getPatients().observe(this, setPatientsObserver())
        verifyLogin()
        setupSearch()
    }

    private fun setupSearch() {
        binding.etSearch.onTextChanged {  query ->
            job?.cancel()
            if (query.isNotBlank()) {
                job = MainScope().launch {
                    delay(SEARCH_USER_TIME_DELAY)
                    val filteredList = viewModel.filterList(allPatient, query)
                    verifyNotEmpty(filteredList)
                    patientAdapter.differ.submitList(null)
                    patientAdapter.differ.submitList(filteredList)
                }
            } else {
                patientAdapter.differ.submitList(allPatient)
                verifyNotEmpty(allPatient)
            }
        }
    }

    private fun verifyLogin() {
        isLoggedIn = viewModel.verifyLogin()
        if (isLoggedIn) {
            binding.fabAddPatient.visible()
            binding.fabAddPatient.setOnClickListener {
                goToActivity(AddPatientActivity::class.java)
            }
        } else {
            binding.fabAddPatient.gone()
        }
        invalidateOptionsMenu()
    }

    private fun verifyNotEmpty(list: List<Patient>) {
        if (list.isEmpty()) binding.tvNoData.visible()
        else binding.tvNoData.gone()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        if (isLoggedIn) {
            menuInflater.inflate(R.menu.menu_profile, menu)
        } else {
            menuInflater.inflate(R.menu.menu_login, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.miLogin -> {
                goToActivity(LoginActivity::class.java)
                return true
            }
            R.id.miProfile -> {
                goToActivity(ProfileActivity::class.java)
                return true
            }
        }

        return false
    }

    private fun setPatientsObserver() = setObserver<List<Patient>>(
        onSuccess = {
            binding.progressBar.gone()
            allPatient = it.data.orEmpty()
            verifyNotEmpty(allPatient)
            patientAdapter.differ.submitList(allPatient)
        },
        onError = {
            binding.progressBar.gone()
            showToast(it.message.orEmpty())
        },
        onLoading = {
            binding.progressBar.visible()
        }
    )

    private fun setLogoutObserver() = setObserver<Unit?>(
        onSuccess = {
            binding.progressBar.gone()
            showToast("Berhasil logout")
            setup()
            invalidateOptionsMenu()
        },
        onError = {
            binding.progressBar.gone()
            showToast(it.message.toString())
        },
        onLoading = { binding.progressBar.visible() }
    )

    private fun setupRecyclerView() {
        with(binding.rvPatient) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = patientAdapter
        }

        patientAdapter.setOnItemClickListener {
            goToActivity(DetailPatientActivity::class.java, Bundle().apply {
                putString(DetailPatientActivity.EXTRA_ID, it.id)
            })
        }
    }

    companion object {
        const val SEARCH_USER_TIME_DELAY = 500L
    }
}