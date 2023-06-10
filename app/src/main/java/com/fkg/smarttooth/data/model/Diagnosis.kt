package com.fkg.smarttooth.data.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import kotlinx.parcelize.Parcelize

@Parcelize
data class Diagnosis(
    val name: String,
    val detail: List<DiagnosisDetail>,
    @DrawableRes val icon: Int
) : ExpandableGroup<DiagnosisDetail>(name, detail), Parcelable
