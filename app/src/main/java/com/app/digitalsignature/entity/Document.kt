package com.app.digitalsignature.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Document(
    var id: Int = 0,
    var fileName: String? = null,
    var size: String? = null,
    var date: String? = null
) : Parcelable