package com.sougata.supplysync.firestore.util

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.sougata.supplysync.models.Customer
import com.sougata.supplysync.models.CustomerPayment
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Order
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.models.UserItem
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
                supplierItemId = map[FieldNames.OrderedItemsCol.ITEM_ID] as String,
                supplierItemName = map[FieldNames.OrderedItemsCol.ITEM_NAME] as String,
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

            Model.CUSTOMER -> Customer(
                name = map[FieldNames.CustomersCol.NAME] as String,
                receivableAmount = Converters.numberToDouble(map[FieldNames.CustomersCol.RECEIVABLE_AMOUNT] as Number),
                dueOrders = Converters.numberToInt(map[FieldNames.CustomersCol.DUE_ORDERS] as Number),
                phone = map[FieldNames.CustomersCol.PHONE] as String,
                email = map[FieldNames.CustomersCol.EMAIL] as String,
                note = map[FieldNames.CustomersCol.NOTE] as String,
                profileImageUrl = map[FieldNames.CustomersCol.PROFILE_IMAGE_URL] as String
            ).apply {
                id = document.id
                timestamp = map[FieldNames.CustomersCol.TIMESTAMP] as Timestamp
            }

            Model.CUSTOMER_PAYMENT -> CustomerPayment(
                amount = Converters.numberToDouble(map[FieldNames.CustomerPaymentsCol.AMOUNT] as Number),
                paymentTimestamp = map[FieldNames.CustomerPaymentsCol.PAYMENT_TIMESTAMP] as Timestamp,
                note = map[FieldNames.CustomerPaymentsCol.NOTE] as String,
                customerId = map[FieldNames.CustomerPaymentsCol.CUSTOMER_ID] as String,
                customerName = map[FieldNames.CustomerPaymentsCol.CUSTOMER_NAME] as String
            ).apply {
                id = document.id
                timestamp = map[FieldNames.CustomerPaymentsCol.TIMESTAMP] as Timestamp
            }

            Model.ORDER -> Order(
                userItemId = map[FieldNames.OrdersCol.USER_ITEM_ID] as String,
                userItemName = map[FieldNames.OrdersCol.USER_ITEM_NAME] as String,
                quantity = Converters.numberToInt(map[FieldNames.OrdersCol.QUANTITY] as Number),
                amount = Converters.numberToDouble(map[FieldNames.OrdersCol.AMOUNT] as Number),
                customerId = map[FieldNames.OrdersCol.CUSTOMER_ID] as String,
                customerName = map[FieldNames.OrdersCol.CUSTOMER_NAME] as String,
                deliveryTimestamp = map[FieldNames.OrdersCol.DELIVERY_TIMESTAMP] as Timestamp,
                isDelivered = map[FieldNames.OrdersCol.IS_DELIVERED] as Boolean
            ).apply {
                id = document.id
                timestamp = map[FieldNames.OrdersCol.TIMESTAMP] as Timestamp
            }

            Model.USER_ITEM -> UserItem(
                name = map[FieldNames.UserItemsCol.NAME] as String,
                inStock = Converters.numberToInt(map[FieldNames.UserItemsCol.IN_STOCK] as Number),
                price = Converters.numberToDouble(map[FieldNames.UserItemsCol.PRICE] as Number),
                details = map[FieldNames.UserItemsCol.DETAILS] as String
            ).apply {
                id = document.id
                timestamp = map[FieldNames.UserItemsCol.TIMESTAMP] as Timestamp
            }

            else -> throw Exception("Invalid model name")
        }
    }

}