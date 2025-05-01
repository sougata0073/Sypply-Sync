package com.sougata.supplysync.cloud

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
                name = map[FieldNamesRepository.SuppliersCollection.NAME] as String,
                dueAmount = Converters.numberToDouble(map[FieldNamesRepository.SuppliersCollection.DUE_AMOUNT] as Number),
                phone = map[FieldNamesRepository.SuppliersCollection.PHONE] as String,
                email = map[FieldNamesRepository.SuppliersCollection.EMAIL] as String,
                note = map[FieldNamesRepository.SuppliersCollection.NOTE] as String,
                paymentDetails = map[FieldNamesRepository.SuppliersCollection.PAYMENT_DETAILS] as String,
                profileImageUrl = map[FieldNamesRepository.SuppliersCollection.PROFILE_IMAGE_URL] as String
            ).apply {
                id = document.id
                timestamp = map[FieldNamesRepository.SuppliersCollection.TIMESTAMP] as Timestamp
            }

            Model.SUPPLIERS_ITEM -> SupplierItem(
                name = map[FieldNamesRepository.SupplierItemsCollection.NAME] as String,
                price = Converters.numberToDouble(map[FieldNamesRepository.SupplierItemsCollection.PRICE] as Number),
                details = map[FieldNamesRepository.SupplierItemsCollection.DETAILS] as String
            ).apply {
                id = document.id
                timestamp = map[FieldNamesRepository.SupplierItemsCollection.TIMESTAMP] as Timestamp
            }

            Model.SUPPLIER_PAYMENT -> SupplierPayment(
                amount = Converters.numberToDouble(map[FieldNamesRepository.SupplierPaymentsCollection.AMOUNT] as Number),
                paymentTimestamp = map[FieldNamesRepository.SupplierPaymentsCollection.PAYMENT_TIMESTAMP] as Timestamp,
                note = map[FieldNamesRepository.SupplierPaymentsCollection.NOTE] as String,
                supplierId = map[FieldNamesRepository.SupplierPaymentsCollection.SUPPLIER_ID] as String,
                supplierName = map[FieldNamesRepository.SupplierPaymentsCollection.SUPPLIER_NAME] as String
            ).apply {
                id = document.id
                timestamp = map[FieldNamesRepository.SupplierPaymentsCollection.TIMESTAMP] as Timestamp
            }

            Model.ORDERED_ITEM -> OrderedItem(
                itemId = map[FieldNamesRepository.OrderedItemsCollection.ITEM_ID] as String,
                itemName = map[FieldNamesRepository.OrderedItemsCollection.ITEM_NAME] as String,
                quantity = Converters.numberToInt(map[FieldNamesRepository.OrderedItemsCollection.QUANTITY] as Number),
                amount = Converters.numberToDouble(map[FieldNamesRepository.OrderedItemsCollection.AMOUNT] as Number),
                supplierId = map[FieldNamesRepository.OrderedItemsCollection.SUPPLIER_ID] as String,
                supplierName = map[FieldNamesRepository.OrderedItemsCollection.SUPPLIER_NAME] as String,
                orderTimestamp = map[FieldNamesRepository.OrderedItemsCollection.ORDER_TIMESTAMP] as Timestamp,
                isReceived = map[FieldNamesRepository.OrderedItemsCollection.IS_RECEIVED] as Boolean
            ).apply {
                id = document.id
                timestamp = map[FieldNamesRepository.OrderedItemsCollection.TIMESTAMP] as Timestamp
            }

            else -> throw Exception("Invalid model name")
        }
    }

}