package com.fkg.smarttooth.ui.inputdata

import android.view.LayoutInflater
import com.fkg.smarttooth.base.BaseActivity
import com.fkg.smarttooth.databinding.ActivityInputDataBinding
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

    companion object {
        const val EXTRA_ID = "extra_id"
    }
}