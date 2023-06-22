package com.fkg.smarttooth.ui.inputdata

import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.fkg.smarttooth.R
import com.fkg.smarttooth.base.BaseActivity
import com.fkg.smarttooth.databinding.ActivityInputDataBinding
import com.fkg.smarttooth.utils.HELP_BODY_INPUT_DATA
import com.fkg.smarttooth.utils.TAB_TITLES
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class InputDataActivity : BaseActivity<ActivityInputDataBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityInputDataBinding =
        ActivityInputDataBinding::inflate

    private var id: String = ""

    override fun setup() {
        setTitle("Data Gigi Pasien")
        setupBackButton()
        id = intent.extras?.getString(EXTRA_ID).orEmpty()
        val sectionPagerAdapter = SectionPagerAdapter(this, id)
        binding.viewPager.adapter = sectionPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = resources.getString(TAB_TITLES[position])
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_help, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.miHelp -> {
                MaterialDialog(this).show {
                    title(text = "Cara Penggunaan Aplikasi")
                    message(text = HELP_BODY_INPUT_DATA) {
                        html()
                    }
                    positiveButton(text = "Tutup")
                }
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return false
    }

    companion object {
        const val EXTRA_ID = "extra_id"
    }
}