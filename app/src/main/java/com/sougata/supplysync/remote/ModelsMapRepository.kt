package com.sougata.supplysync.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.util.Converters

class ModelsMapRepository {

    fun getMappedModel(
        modelName: String,
        map: Map<String, Any>,
        document: DocumentSnapshot
    ): Model {

        return when (modelName) {
            Model.SUPPLIER -> Supplier(
                name = map[FirestoreFieldNames.SuppliersCol.NAME] as String,
                dueAmount = Converters.numberToDouble(map[FirestoreFieldNames.SuppliersCol.DUE_AMOUNT] as Number),
                phone = map[FirestoreFieldNames.SuppliersCol.PHONE] as String,
                email = map[FirestoreFieldNames.SuppliersCol.EMAIL] as String,
                note = map[FirestoreFieldNames.SuppliersCol.NOTE] as String,
                paymentDetails = map[FirestoreFieldNames.SuppliersCol.PAYMENT_DETAILS] as String,
                profileImageUrl = map[FirestoreFieldNames.SuppliersCol.PROFILE_IMAGE_URL] as String
            ).apply {
                id = document.id
                timestamp = map[FirestoreFieldNames.SuppliersCol.TIMESTAMP] as Timestamp
            }

            Model.SUPPLIERS_ITEM -> SupplierItem(
                name = map[FirestoreFieldNames.SupplierItemsCol.NAME] as String,
                price = Converters.numberToDouble(map[FirestoreFieldNames.SupplierItemsCol.PRICE] as Number),
                details = map[FirestoreFieldNames.SupplierItemsCol.DETAILS] as String
            ).apply {
                id = document.id
                timestamp = map[FirestoreFieldNames.SupplierItemsCol.TIMESTAMP] as Timestamp
            }

            Model.SUPPLIER_PAYMENT -> SupplierPayment(
                amount = Converters.numberToDouble(map[FirestoreFieldNames.SupplierPaymentsCol.AMOUNT] as Number),
                paymentTimestamp = map[FirestoreFieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP] as Timestamp,
                note = map[FirestoreFieldNames.SupplierPaymentsCol.NOTE] as String,
                supplierId = map[FirestoreFieldNames.SupplierPaymentsCol.SUPPLIER_ID] as String,
                supplierName = map[FirestoreFieldNames.SupplierPaymentsCol.SUPPLIER_NAME] as String
            ).apply {
                id = document.id
                timestamp = map[FirestoreFieldNames.SupplierPaymentsCol.TIMESTAMP] as Timestamp
            }

            Model.ORDERED_ITEM -> OrderedItem(
                itemId = map[FirestoreFieldNames.OrderedItemsCol.ITEM_ID] as String,
                itemName = map[FirestoreFieldNames.OrderedItemsCol.ITEM_NAME] as String,
                quantity = Converters.numberToInt(map[FirestoreFieldNames.OrderedItemsCol.QUANTITY] as Number),
                amount = Converters.numberToDouble(map[FirestoreFieldNames.OrderedItemsCol.AMOUNT] as Number),
                supplierId = map[FirestoreFieldNames.OrderedItemsCol.SUPPLIER_ID] as String,
                supplierName = map[FirestoreFieldNames.OrderedItemsCol.SUPPLIER_NAME] as String,
                orderTimestamp = map[FirestoreFieldNames.OrderedItemsCol.ORDER_TIMESTAMP] as Timestamp,
                isReceived = map[FirestoreFieldNames.OrderedItemsCol.IS_RECEIVED] as Boolean
            ).apply {
                id = document.id
                timestamp = map[FirestoreFieldNames.OrderedItemsCol.TIMESTAMP] as Timestamp
            }

            else -> throw Exception("Invalid model name")
        }
    }

}