package com.fkg.smarttooth.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiagnosisDetail(
    val title: String,
    val result: String,
    val explanation: String
) : Parcelable
