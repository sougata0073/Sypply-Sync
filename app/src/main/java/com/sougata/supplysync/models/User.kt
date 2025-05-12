package com.sougata.supplysync.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    override var id: String,
    override var timestamp: Timestamp,
    var name: String,
    var email: String,
    var phone: String,
    var uid: String = ""
): Model, Parcelable {

    constructor() : this("", Timestamp.now(), "", "", "", "")

    override fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "timestamp" to timestamp,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "uid" to uid
        )
    }

    override fun toModel(map: Map<String, Any>): Model {
        return User(
            id = map["id"] as String,
            timestamp = map["timestamp"] as Timestamp,
            name = map["name"] as String,
            email = map["email"] as String,
            phone = map["phone"] as String,
            uid = map["uid"] as String
        )
    }
}