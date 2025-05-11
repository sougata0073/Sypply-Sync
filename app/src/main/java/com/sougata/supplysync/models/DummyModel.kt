package com.sougata.supplysync.models

import com.google.firebase.Timestamp
import java.util.UUID

data class DummyModel(
    override var id: String = UUID.randomUUID().toString(),
    override var timestamp: Timestamp = Timestamp.now(),
) : Model {
    override fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "timestamp" to timestamp,
        )
    }

    override fun toModel(map: Map<String, Any>): Model {
        return DummyModel(
            id = map["id"] as String,
            timestamp = map["timestamp"] as Timestamp,
        )
    }

}
