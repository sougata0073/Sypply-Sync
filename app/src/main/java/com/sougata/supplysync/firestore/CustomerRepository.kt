package com.sougata.supplysync.firestore

import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sougata.supplysync.firestore.util.Action
import com.sougata.supplysync.firestore.util.FieldNames
import com.sougata.supplysync.firestore.util.Helper
import com.sougata.supplysync.firestore.util.ModelMaps
import com.sougata.supplysync.models.Customer
import com.sougata.supplysync.models.CustomerPayment
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.Order
import com.sougata.supplysync.models.UserItem
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status

class CustomerRepository {

    private val currentUser = Firebase.auth.currentUser!!
    private val db = Firebase.firestore

    private val usersCol =
        this.db.collection(FieldNames.UsersCol.SELF_NAME)

    private val helper = Helper()
    private val modelMaps = ModelMaps()

    fun getCustomersList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.CUSTOMER, map, document)
        }

        this.helper.getAnyModelsList(
            firebaseCollectionName = FieldNames.CustomersCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.CustomersCol.TIMESTAMP to Query.Direction.ASCENDING,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getCustomersListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.CUSTOMER, map, document)
        }

        this.helper.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FieldNames.CustomersCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getCustomerPaymentsList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.CUSTOMER_PAYMENT, map, document)
        }

        this.helper.getAnyModelsList(
            firebaseCollectionName = FieldNames.CustomerPaymentsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.CustomerPaymentsCol.PAYMENT_TIMESTAMP to Query.Direction.DESCENDING,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getCustomerPaymentsListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.CUSTOMER_PAYMENT, map, document)
        }

        this.helper.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FieldNames.CustomerPaymentsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getOrdersList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.ORDER, map, document)
        }

        this.helper.getAnyModelsList(
            firebaseCollectionName = FieldNames.OrdersCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.OrdersCol.TIMESTAMP to Query.Direction.DESCENDING,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getOrdersListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.ORDER, map, document)
        }

        this.helper.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FieldNames.OrdersCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getUserItemsList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.USER_ITEM, map, document)
        }

        this.helper.getAnyModelsList(
            firebaseCollectionName = FieldNames.UserItemsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.UserItemsCol.TIMESTAMP to Query.Direction.ASCENDING,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getUserItemsListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.USER_ITEM, map, document)
        }

        this.helper.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FieldNames.UserItemsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun addUpdateCustomer(
        customer: Customer, action: Action, onComplete: (Status, String) -> Unit
    ) {
        val customersCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.CustomersCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.ValuesCol.SELF_NAME)

        val customerDoc = mutableMapOf<String, Any>(
            FieldNames.CustomersCol.NAME to customer.name,
            FieldNames.CustomersCol.RECEIVABLE_AMOUNT to customer.receivableAmount,
            FieldNames.CustomersCol.DUE_ORDERS to customer.dueOrders,
            FieldNames.CustomersCol.PHONE to customer.phone,
            FieldNames.CustomersCol.EMAIL to customer.email,
            FieldNames.CustomersCol.NOTE to customer.note,
            FieldNames.CustomersCol.PROFILE_IMAGE_URL to customer.profileImageUrl
        )

        this.usersCol.firestore.runTransaction {
            if (action == Action.TO_ADD) {
                customerDoc.put(
                    FieldNames.CustomersCol.TIMESTAMP,
                    FieldValue.serverTimestamp()
                )
                it.set(customersCol.document(), customerDoc)
                it.update(
                    valuesCol.document(FieldNames.ValuesCol.CustomersCountDoc.SELF_NAME),
                    mapOf(
                        FieldNames.ValuesCol.CustomersCountDoc.VALUE to FieldValue.increment(
                            1
                        )
                    )
                )
                it.update(
                    valuesCol.document(FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.SELF_NAME),
                    mapOf(
                        FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.VALUE to FieldValue.increment(
                            customer.receivableAmount
                        )
                    )
                )
            } else if (action == Action.TO_UPDATE) {
                var prevTotalReceivableAmount =
                    it.get(valuesCol.document(FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.SELF_NAME))
                        .getDouble(FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.VALUE)
                        ?: 0.0
                var prevCustomerReceivableAmount =
                    it.get(customersCol.document(customer.id))
                        .getDouble(FieldNames.CustomersCol.RECEIVABLE_AMOUNT) ?: 0.0
                val newTotalReceivableAmount: Double? =
                    prevTotalReceivableAmount - prevCustomerReceivableAmount + customer.receivableAmount
                it.update(customersCol.document(customer.id), customerDoc)
                it.update(
                    valuesCol.document(FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.SELF_NAME),
                    mapOf(FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.VALUE to newTotalReceivableAmount)
                )
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }

    fun addUpdateUserItem(
        userItem: UserItem, action: Action, onComplete: (Status, String) -> Unit
    ) {
        val userItemsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FieldNames.UserItemsCol.SELF_NAME)

        val userItemDoc = mutableMapOf<String, Any>(
            FieldNames.UserItemsCol.NAME to userItem.name,
            FieldNames.UserItemsCol.IN_STOCK to userItem.inStock,
            FieldNames.UserItemsCol.PRICE to userItem.price,
            FieldNames.UserItemsCol.DETAILS to userItem.details,
        )

        this.usersCol.firestore.runTransaction {
            if (action == Action.TO_ADD) {
                userItemDoc.put(
                    FieldNames.UserItemsCol.TIMESTAMP,
                    FieldValue.serverTimestamp()
                )
                it.set(userItemsCol.document(), userItemDoc)
            } else if (action == Action.TO_UPDATE) {
                it.update(userItemsCol.document(userItem.id), userItemDoc)
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun addUpdateCustomerPayment(
        customerPayment: CustomerPayment, action: Action, onComplete: (Status, String) -> Unit
    ) {

        val customerPaymentsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FieldNames.CustomerPaymentsCol.SELF_NAME)

        val customerPaymentDoc = mutableMapOf<String, Any>(
            FieldNames.CustomerPaymentsCol.AMOUNT to customerPayment.amount,
            FieldNames.CustomerPaymentsCol.PAYMENT_TIMESTAMP to customerPayment.paymentTimestamp,
            FieldNames.CustomerPaymentsCol.NOTE to customerPayment.note,
            FieldNames.CustomerPaymentsCol.CUSTOMER_ID to customerPayment.customerId,
            FieldNames.CustomerPaymentsCol.CUSTOMER_NAME to customerPayment.customerName,
        )

        this.usersCol.firestore.runTransaction {
            if (action == Action.TO_ADD) {
                customerPaymentDoc.put(
                    FieldNames.CustomerPaymentsCol.TIMESTAMP,
                    FieldValue.serverTimestamp()
                )
                it.set(customerPaymentsCol.document(), customerPaymentDoc)
            } else if (action == Action.TO_UPDATE) {
                it.update(customerPaymentsCol.document(customerPayment.id), customerPaymentDoc)
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }

    fun addUpdateOrder(
        order: Order, action: Action, onComplete: (Status, String) -> Unit
    ) {
        val ordersCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FieldNames.OrdersCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.ValuesCol.SELF_NAME)

        val orderDoc = mutableMapOf<String, Any>(
            FieldNames.OrdersCol.USER_ITEM_ID to order.userItemId,
            FieldNames.OrdersCol.USER_ITEM_NAME to order.userItemName,
            FieldNames.OrdersCol.QUANTITY to order.quantity,
            FieldNames.OrdersCol.AMOUNT to order.amount,
            FieldNames.OrdersCol.CUSTOMER_ID to order.customerId,
            FieldNames.OrdersCol.CUSTOMER_NAME to order.customerName,
            FieldNames.OrdersCol.DELIVERY_TIMESTAMP to order.deliveryTimestamp,
            FieldNames.OrdersCol.IS_DELIVERED to order.isDelivered,
        )

        this.usersCol.firestore.runTransaction {
            if (action == Action.TO_ADD) {
                orderDoc.put(
                    FieldNames.OrdersCol.TIMESTAMP,
                    FieldValue.serverTimestamp()
                )
                it.set(ordersCol.document(), orderDoc)
                if (!order.isDelivered) {
                    it.update(
                        valuesCol.document(FieldNames.ValuesCol.OrdersToDeliverDoc.SELF_NAME),
                        mapOf(
                            FieldNames.ValuesCol.OrdersToDeliverDoc.VALUE to FieldValue.increment(
                                1
                            )
                        )
                    )
                }
            } else if (action == Action.TO_UPDATE) {
                val prevIsDelivered = it.get(ordersCol.document(order.id))
                    .getBoolean(FieldNames.OrdersCol.IS_DELIVERED) ?: false

                if (prevIsDelivered == true && order.isDelivered == false) {
                    it.update(
                        valuesCol.document(FieldNames.ValuesCol.OrdersToDeliverDoc.SELF_NAME),
                        mapOf(
                            FieldNames.ValuesCol.OrdersToDeliverDoc.VALUE to FieldValue.increment(
                                1
                            )
                        )
                    )
                } else if (prevIsDelivered == false && order.isDelivered == true) {
                    it.update(
                        valuesCol.document(FieldNames.ValuesCol.OrdersToDeliverDoc.SELF_NAME),
                        mapOf(
                            FieldNames.ValuesCol.OrdersToDeliverDoc.VALUE to FieldValue.increment(
                                -1
                            )
                        )
                    )
                }
                it.update(ordersCol.document(order.id), orderDoc)
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getOrdersToDeliver(onComplete: (Status, Number, String) -> Unit) {
        this.helper.getAnyValueFromValuesCol(
            FieldNames.ValuesCol.OrdersToDeliverDoc.SELF_NAME,
            FirestoreFieldDataType.NUMBER,
            onComplete as (Status, Any, String) -> Unit
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun getNumberOfCustomers(onComplete: (Status, Number, String) -> Unit) {
        this.helper.getAnyValueFromValuesCol(
            FieldNames.ValuesCol.CustomersCountDoc.SELF_NAME,
            FirestoreFieldDataType.NUMBER,
            onComplete as (Status, Any, String) -> Unit
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun getReceivableAmountFromCustomers(onComplete: (Status, Number, String) -> Unit) {
        this.helper.getAnyValueFromValuesCol(
            FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.SELF_NAME,
            FirestoreFieldDataType.NUMBER,
            onComplete as (Status, Any, String) -> Unit
        )
    }

    fun deleteCustomer(customer: Customer, onComplete: (Status, String) -> Unit) {
        val customersCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.CustomersCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.ValuesCol.SELF_NAME)

        this.usersCol.firestore.runTransaction {
            it.delete(customersCol.document(customer.id))
            it.update(
                valuesCol.document(FieldNames.ValuesCol.CustomersCountDoc.SELF_NAME),
                mapOf(
                    FieldNames.ValuesCol.CustomersCountDoc.VALUE to FieldValue.increment(
                        -1
                    )
                )
            )
            it.update(
                valuesCol.document(FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.SELF_NAME),
                mapOf(
                    FieldNames.ValuesCol.ReceivableAmountFromCustomersDoc.VALUE to FieldValue.increment(
                        -customer.receivableAmount
                    )
                )
            )
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun deleteOrder(order: Order, onComplete: (Status, String) -> Unit) {
        val ordersCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.OrdersCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.ValuesCol.SELF_NAME)

        this.usersCol.firestore.runTransaction {
            it.delete(ordersCol.document(order.id))
            if (!order.isDelivered) {
                it.update(
                    valuesCol.document(FieldNames.ValuesCol.OrdersToDeliverDoc.SELF_NAME),
                    mapOf(
                        FieldNames.ValuesCol.OrdersToDeliverDoc.VALUE to FieldValue.increment(
                            -1
                        )
                    )
                )
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun deleteCustomerPayment(
        customerPayment: CustomerPayment,
        onComplete: (Status, String) -> Unit
    ) {
        val customerPaymentsCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.CustomerPaymentsCol.SELF_NAME)
        this.usersCol.firestore.runTransaction {
            it.delete(customerPaymentsCol.document(customerPayment.id))

        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun deleteUserItem(userItem: UserItem, onComplete: (Status, String) -> Unit) {
        val userItemsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FieldNames.UserItemsCol.SELF_NAME)

        this.usersCol.firestore.runTransaction {
            it.delete(userItemsCol.document(userItem.id))
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun getSalesAmountByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        onComplete: (Status, Number, String) -> Unit
    ) {
        val ordersCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.OrdersCol.SELF_NAME)

        val query = ordersCol
            .whereGreaterThanOrEqualTo(
                FieldNames.OrdersCol.TIMESTAMP,
                startTimestamp
            )
            .whereLessThanOrEqualTo(
                FieldNames.OrdersCol.TIMESTAMP,
                endTimestamp
            ).whereEqualTo(
                FieldNames.OrdersCol.IS_DELIVERED,
                true
            ).aggregate(AggregateField.sum(FieldNames.OrdersCol.AMOUNT))

        query.get(AggregateSource.SERVER).addOnCompleteListener {
            if (it.isSuccessful) {
                val sum =
                    it.result.get(AggregateField.sum(FieldNames.OrdersCol.AMOUNT)) as? Number

                if (sum != null) {
                    onComplete(
                        Status.SUCCESS,
                        Converters.numberToDouble(sum),
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                } else {
                    onComplete(Status.FAILED, 0.0, KeysAndMessages.TASK_FAILED_TO_COMPLETE)
                }
            } else {
                onComplete(Status.FAILED, 0.0, it.exception?.message.toString())
            }
        }
    }

}