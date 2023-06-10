package com.fkg.smarttooth.ui.main

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.RotateAnimation
import com.fkg.smarttooth.data.model.Diagnosis
import com.fkg.smarttooth.databinding.ItemDiagnosisBinding
import com.fkg.smarttooth.databinding.ItemDiagnosisDetailBinding
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder


class DiagnosisAdapter(
    groups: List<Diagnosis>
) : ExpandableRecyclerViewAdapter<DiagnosisAdapter.DiagnosisViewHolder, DiagnosisAdapter.DetailViewHolder>(groups) {

    override fun onCreateGroupViewHolder(parent: ViewGroup?, viewType: Int): DiagnosisViewHolder {
        val itemBinding = ItemDiagnosisBinding.inflate(LayoutInflater.from(parent?.context), parent,false)
        return DiagnosisViewHolder(itemBinding)
    }

    override fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): DetailViewHolder {
        val itemBinding = ItemDiagnosisDetailBinding.inflate(LayoutInflater.from(parent?.context), parent,false)
        return DetailViewHolder(itemBinding)
    }

    override fun onBindGroupViewHolder(
        holder: DiagnosisViewHolder?,
        flatPosition: Int,
        group: ExpandableGroup<*>?
    ) {
        if (group == null || holder == null) {
            Log.d("coba", "onBindGroupViewHolder: error binding group vh")
            return
        }

        val item = group as Diagnosis
        with(holder.binding) {
            tvTitle.text = item.name
            imgDiagnosis.setImageDrawable(holder.binding.root.resources.getDrawable(item.icon))
        }
    }

    override fun onBindChildViewHolder(
        holder: DetailViewHolder?,
        flatPosition: Int,
        group: ExpandableGroup<*>?,
        childIndex: Int
    ) {
        if (group == null || holder == null) {
            Log.d("coba", "onBindGroupViewHolder: error binding child vh")
            return
        }

        val item = (group as Diagnosis).items[childIndex]
        with(holder.binding) {
            tvTitle.text = "Diagnosis ${item.title}"
            tvResult.text = item.result
            tvExplanation.text = item.explanation
        }
    }

    inner class DiagnosisViewHolder(
        val binding: ItemDiagnosisBinding
    ): GroupViewHolder(binding.root) {
        override fun expand() {
            animateExpand()
        }

        override fun collapse() {
            animateCollapse()
        }

        private fun animateExpand() {
            val rotate = RotateAnimation(360f, 180f, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f)
            rotate.duration = 300
            rotate.fillAfter = true
            binding.imgArrow.animation = rotate
        }

        private fun animateCollapse() {
            val rotate = RotateAnimation(180f, 360f, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f)
            rotate.duration = 300
            rotate.fillAfter = true
            binding.imgArrow.animation = rotate
        }
    }

    inner class DetailViewHolder(
        val binding: ItemDiagnosisDetailBinding
    ) : ChildViewHolder(binding.root)
}