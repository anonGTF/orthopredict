package com.fkg.smarttooth.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import com.fkg.smarttooth.base.BaseAdapter
import com.fkg.smarttooth.data.model.Patient
import com.fkg.smarttooth.databinding.ItemPatientBinding

class PatientAdapter : BaseAdapter<ItemPatientBinding, Patient>() {
    override val bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> ItemPatientBinding =
        ItemPatientBinding::inflate

    override fun bind(binding: ItemPatientBinding, item: Patient) {
        with(binding) {
            tvName.text = item.name
            tvAgeGender.text = "${item.gender} - ${item.age} Tahun"
        }
    }
}