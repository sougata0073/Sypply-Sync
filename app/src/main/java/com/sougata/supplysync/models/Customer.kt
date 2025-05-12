package com.sougata.supplysync.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Customer(
    override var id: String,
    override var timestamp: Timestamp,
    var name: String,
    var receivableAmount: Double,
    var dueOrders: Int,
    var phone: String,
    var email: String,
    var note: String,
    var profileImageUrl: String,

): Model, Parcelable {

    constructor() : this("", Timestamp.now(), "", 0.0, 0, "", "", "", "")

    override fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "timestamp" to timestamp,
            "name" to name,
            "receivableAmount" to receivableAmount,
            "dueOrders" to dueOrders,
            "phone" to phone,
            "email" to email,
            "note" to note,
            "profileImageUrl" to profileImageUrl
        )
    }

    override fun toModel(map: Map<String, Any>): Model {
        return Customer(
            id = map["id"] as String,
            timestamp = map["timestamp"] as Timestamp,
            name = map["name"] as String,
            receivableAmount = map["receivableAmount"] as Double,
            dueOrders = map["dueOrders"] as Int,
            phone = map["phone"] as String,
            email = map["email"] as String,
            note = map["note"] as String,
            profileImageUrl = map["profileImageUrl"] as String
        )
    }

}
