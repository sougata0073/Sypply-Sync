package com.sougata.supplysync.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var name: String,
    var email: String,
    var phone: String,
    var uid: String = ""
): Model(), Parcelable