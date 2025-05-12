package com.sougata.supplysync.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Supplier(
    override var id: String,
    override var timestamp: Timestamp,
    var name: String,
    var dueAmount: Double,
    var phone: String,
    var email: String,
    var note: String,
    var paymentDetails: String,
    var profileImageUrl: String
) : Model, Parcelable {

    constructor() : this("", Timestamp.now(), "", 0.0, "", "", "", "", "")

    override fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "timestamp" to timestamp,
            "name" to name,
            "dueAmount" to dueAmount,
            "phone" to phone,
            "email" to email,
            "note" to note,
            "paymentDetails" to paymentDetails,
            "profileImageUrl" to profileImageUrl
        )
    }

    override fun toModel(map: Map<String, Any>): Model {
        return Supplier(
            id = map["id"] as String,
            timestamp = map["timestamp"] as Timestamp,
            name = map["name"] as String,
            dueAmount = map["dueAmount"] as Double,
            phone = map["phone"] as String,
            email = map["email"] as String,
            note = map["note"] as String,
            paymentDetails = map["paymentDetails"] as String,
            profileImageUrl = map["profileImageUrl"] as String
        )
    }
}
