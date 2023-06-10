package com.fkg.smarttooth.ui.profile

import android.view.LayoutInflater
import androidx.activity.viewModels
import com.fkg.smarttooth.base.BaseActivity
import com.fkg.smarttooth.data.model.User
import com.fkg.smarttooth.databinding.ActivityProfileBinding
import com.fkg.smarttooth.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : BaseActivity<ActivityProfileBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityProfileBinding =
        ActivityProfileBinding::inflate

    private val viewModel : ProfileViewModel by viewModels()

    override fun setup() {
        setTitle("Profil")
        setupBackButton()
        viewModel.getUser().observe(this, setUserObserver())

        binding.btnLogout.setOnClickListener {
            viewModel.logout().observe(this, setLogoutObserver())
        }
    }

    private fun setUserObserver() = setObserver<User?>(
        onSuccess = {
            binding.progressBar.gone()
            val user = it.data
            binding.tvName.text = user?.name
            binding.tvInstitution.text = user?.institution
        },
        onError = {
            binding.progressBar.gone()
            showToast(it.message.toString())
        },
        onLoading = { binding.progressBar.visible() }
    )

    private fun setLogoutObserver() = setObserver<Unit?>(
        onSuccess = {
            binding.progressBar.gone()
            goToActivity(MainActivity::class.java, isFinish = true)
        },
        onError = {
            binding.progressBar.gone()
            showToast(it.message.toString())
        },
        onLoading = { binding.progressBar.visible() }
    )
}