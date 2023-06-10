package com.fkg.smarttooth.ui.auth

import android.view.LayoutInflater
import androidx.activity.viewModels
import com.afollestad.vvalidator.form
import com.fkg.smarttooth.base.BaseActivity
import com.fkg.smarttooth.databinding.ActivityRegisterBinding
import com.fkg.smarttooth.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : BaseActivity<ActivityRegisterBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityRegisterBinding =
        ActivityRegisterBinding::inflate

    private val viewModel: AuthViewModel by viewModels()

    override fun setup() {
        form {
            useRealTimeValidation()
            input(binding.etName, name = null) {
                isNotEmpty().description("Nama wajib diisi")
            }

            input(binding.etInstitution, name = null) {
                isNotEmpty().description("Institusi wajib diisi")
            }

            input(binding.etEmail, name = null) {
                isNotEmpty().description("Email wajib diisi")
                isEmail().description("Silahkan masukan email yang valid!")
            }

            input(binding.etPassword, name = null) {
                isNotEmpty().description("Password wajib diisi")
            }

            submitWith(binding.btnRegister) {
                viewModel.register(
                    binding.etName.text.toString(),
                    binding.etInstitution.text.toString(),
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString()
                ).observe(this@RegisterActivity, setRegisterObserver())
            }
        }

        binding.tvLogin.setOnClickListener {
            goToActivity(LoginActivity::class.java)
        }
    }

    private fun setRegisterObserver() = setObserver<Void?>(
        onSuccess = {
            binding.progressBar.gone()
            showToast("Registrasi berhasil")
            goToActivity(MainActivity::class.java)
            finish()
        },
        onError = {
            binding.progressBar.gone()
            showToast(it.message.toString())
        },
        onLoading = { binding.progressBar.visible() }
    )
}