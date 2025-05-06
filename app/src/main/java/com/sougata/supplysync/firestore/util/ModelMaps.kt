package com.sougata.supplysync.firestore.util

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.util.Converters

class ModelMaps {

    fun getMappedModel(
        modelName: String,
        map: Map<String, Any>,
        document: DocumentSnapshot
    ): Model {

        return when (modelName) {
            Model.SUPPLIER -> Supplier(
                name = map[FieldNames.SuppliersCol.NAME] as String,
                dueAmount = Converters.numberToDouble(map[FieldNames.SuppliersCol.DUE_AMOUNT] as Number),
                phone = map[FieldNames.SuppliersCol.PHONE] as String,
                email = map[FieldNames.SuppliersCol.EMAIL] as String,
                note = map[FieldNames.SuppliersCol.NOTE] as String,
                paymentDetails = map[FieldNames.SuppliersCol.PAYMENT_DETAILS] as String,
                profileImageUrl = map[FieldNames.SuppliersCol.PROFILE_IMAGE_URL] as String
            ).apply {
                id = document.id
                timestamp = map[FieldNames.SuppliersCol.TIMESTAMP] as Timestamp
            }

            Model.SUPPLIERS_ITEM -> SupplierItem(
                name = map[FieldNames.SupplierItemsCol.NAME] as String,
                price = Converters.numberToDouble(map[FieldNames.SupplierItemsCol.PRICE] as Number),
                details = map[FieldNames.SupplierItemsCol.DETAILS] as String
            ).apply {
                id = document.id
                timestamp = map[FieldNames.SupplierItemsCol.TIMESTAMP] as Timestamp
            }

            Model.SUPPLIER_PAYMENT -> SupplierPayment(
                amount = Converters.numberToDouble(map[FieldNames.SupplierPaymentsCol.AMOUNT] as Number),
                paymentTimestamp = map[FieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP] as Timestamp,
                note = map[FieldNames.SupplierPaymentsCol.NOTE] as String,
                supplierId = map[FieldNames.SupplierPaymentsCol.SUPPLIER_ID] as String,
                supplierName = map[FieldNames.SupplierPaymentsCol.SUPPLIER_NAME] as String
            ).apply {
                id = document.id
                timestamp = map[FieldNames.SupplierPaymentsCol.TIMESTAMP] as Timestamp
            }

            Model.ORDERED_ITEM -> OrderedItem(
                itemId = map[FieldNames.OrderedItemsCol.ITEM_ID] as String,
                itemName = map[FieldNames.OrderedItemsCol.ITEM_NAME] as String,
                quantity = Converters.numberToInt(map[FieldNames.OrderedItemsCol.QUANTITY] as Number),
                amount = Converters.numberToDouble(map[FieldNames.OrderedItemsCol.AMOUNT] as Number),
                supplierId = map[FieldNames.OrderedItemsCol.SUPPLIER_ID] as String,
                supplierName = map[FieldNames.OrderedItemsCol.SUPPLIER_NAME] as String,
                orderTimestamp = map[FieldNames.OrderedItemsCol.ORDER_TIMESTAMP] as Timestamp,
                isReceived = map[FieldNames.OrderedItemsCol.IS_RECEIVED] as Boolean
            ).apply {
                id = document.id
                timestamp = map[FieldNames.OrderedItemsCol.TIMESTAMP] as Timestamp
            }

            else -> throw Exception("Invalid model name")
        }
    }

}