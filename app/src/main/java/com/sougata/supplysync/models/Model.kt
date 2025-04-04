package com.sougata.supplysync.models

import com.google.firebase.Timestamp

open class Model {

    var id: String = ""
    var timestamp: Timestamp = Timestamp.now()

    companion object {
        const val SUPPLIER = "Supplier"
        const val USER = "User"
        const val SUPPLIERS_ITEM = "Supplier item"
        const val SUPPLIER_PAYMENT = "Supplier payment"
        const val ORDERED_ITEM = "Ordered item"
    }
}