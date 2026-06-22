package com.crayon.fieldapp.ui.screen.detailProject.addStore.adapter

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ItemSelectMember(
    val id: String,
    val name: String? = null,
    val avatar: String? = null,
    var isSelected: Boolean = false
) : Parcelable {}