package com.fkg.smarttooth.ui.inputdata

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fkg.smarttooth.utils.TAB_TITLES

class SectionPagerAdapter(activity: FragmentActivity, val id: String) :
    FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = TAB_TITLES.size
    override fun createFragment(position: Int): Fragment = InputDataFragment(TAB_TITLES[position], id)
}