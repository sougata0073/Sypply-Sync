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
import com.sougata.supplysync.firestore.util.FieldNames
import com.sougata.supplysync.firestore.util.HelperRepository
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.models.User
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SupplierRepository {

    private val currentUser = Firebase.auth.currentUser!!
    private val db = Firebase.firestore

    private val usersCol =
        this.db.collection(FieldNames.UsersCol.SELF_NAME)
    private val currentUserDoc = this.usersCol.document(this.currentUser.uid)

    private val valuesCol = this.currentUserDoc.collection(FieldNames.ValuesCol.SELF_NAME)

    private val suppliersCol = this.currentUserDoc.collection(FieldNames.SuppliersCol.SELF_NAME)
    private val supplierItemsCol =
        this.currentUserDoc.collection(FieldNames.SupplierItemsCol.SELF_NAME)
    val supplierPaymentsCol =
        this.currentUserDoc.collection(FieldNames.SupplierPaymentsCol.SELF_NAME)
    val orderedItemsCol =
        this.currentUserDoc.collection(FieldNames.OrderedItemsCol.SELF_NAME)

    private val helperRepository = HelperRepository()

    fun getSuppliersList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.getAnyModelsList(
            firebaseCollectionName = FieldNames.SuppliersCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.SuppliersCol.TIMESTAMP to Query.Direction.ASCENDING,
            limit = limit,
            clazz = Supplier::class.java,
            onComplete = onComplete
        )
    }

    fun getSuppliersListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FieldNames.SuppliersCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            clazz = Supplier::class.java,
            onComplete = onComplete
        )
    }

    fun getSupplierItemsList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.getAnyModelsList(
            firebaseCollectionName = FieldNames.SupplierItemsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.SupplierItemsCol.TIMESTAMP to Query.Direction.ASCENDING,
            limit = limit,
            clazz = SupplierItem::class.java,
            onComplete = onComplete
        )
    }

    fun getSupplierItemsListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FieldNames.SupplierItemsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            clazz = SupplierItem::class.java,
            onComplete = onComplete
        )
    }

    fun getOrderedItemsList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.getAnyModelsList(
            firebaseCollectionName = FieldNames.OrderedItemsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.OrderedItemsCol.TIMESTAMP to Query.Direction.ASCENDING,
            limit = limit,
            clazz = OrderedItem::class.java,
            onComplete = onComplete
        )
    }

    fun getOrderedItemsListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FieldNames.OrderedItemsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            clazz = OrderedItem::class.java,
            onComplete = onComplete
        )
    }

    fun getSupplierPaymentsList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.getAnyModelsList(
            firebaseCollectionName = FieldNames.SupplierPaymentsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP to Query.Direction.DESCENDING,
            limit = limit,
            clazz = SupplierPayment::class.java,
            onComplete = onComplete
        )
    }

    fun getSupplierPaymentsListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FieldNames.SupplierPaymentsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            clazz = SupplierPayment::class.java,
            onComplete = onComplete
        )
    }

    fun addSupplier(supplier: Supplier, onComplete: (Status, String) -> Unit) {

        this.usersCol.firestore.runTransaction {
            it.set(this.suppliersCol.document(), supplier)
            it.update(
                this.valuesCol.document(FieldNames.ValuesCol.SuppliersCountDoc.SELF_NAME),
                mapOf(
                    FieldNames.ValuesCol.SuppliersCountDoc.VALUE to FieldValue.increment(
                        1
                    )
                )
            )
            it.update(
                this.valuesCol.document(FieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME),
                mapOf(
                    FieldNames.ValuesCol.SuppliersDueAmountDoc.VALUE to FieldValue.increment(
                        supplier.dueAmount
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

    fun updateSupplier(supplier: Supplier, onComplete: (Status, String) -> Unit) {

        this.usersCol.firestore.runTransaction {
            var prevTotalDueAmount =
                it.get(this.valuesCol.document(FieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME))
                    .getDouble(FieldNames.ValuesCol.SuppliersDueAmountDoc.VALUE)
                    ?: 0.0
            var prevSupplierDueAmount =
                it.get(this.suppliersCol.document(supplier.id))
                    .getDouble(FieldNames.SuppliersCol.DUE_AMOUNT) ?: 0.0

            val newTotalDueAmount: Double? =
                prevTotalDueAmount - prevSupplierDueAmount + supplier.dueAmount

            it.update(this.suppliersCol.document(supplier.id), supplier.toMap())
            it.update(
                this.valuesCol.document(FieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME),
                mapOf(FieldNames.ValuesCol.SuppliersDueAmountDoc.VALUE to newTotalDueAmount)
            )
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun deleteSupplier(supplier: Supplier, onComplete: (Status, String) -> Unit) {
        this.usersCol.firestore.runTransaction {
            it.delete(this.suppliersCol.document(supplier.id))
            it.update(
                this.valuesCol.document(FieldNames.ValuesCol.SuppliersCountDoc.SELF_NAME),
                mapOf(
                    FieldNames.ValuesCol.SuppliersCountDoc.VALUE to FieldValue.increment(
                        -1
                    )
                )
            )
            it.update(
                this.valuesCol.document(FieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME),
                mapOf(
                    FieldNames.ValuesCol.SuppliersDueAmountDoc.VALUE to FieldValue.increment(
                        -supplier.dueAmount
                    )
                )
            )
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, "Supplier deleted successfully")
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun addSupplierItem(supplierItem: SupplierItem, onComplete: (Status, String) -> Unit) {
        this.usersCol.firestore.runTransaction {
            it.set(this.supplierItemsCol.document(), supplierItem)
            it.update(
                this.valuesCol.document(FieldNames.ValuesCol.SupplierItemsCountDoc.SELF_NAME),
                mapOf(
                    FieldNames.ValuesCol.SupplierItemsCountDoc.VALUE to FieldValue.increment(
                        1
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

    fun updateSupplierItem(supplierItem: SupplierItem, onComplete: (Status, String) -> Unit) {
        this.supplierItemsCol.document(supplierItem.id).update(supplierItem.toMap())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
                } else {
                    onComplete(Status.FAILED, it.exception?.message.toString())
                }
            }
    }

    fun deleteSupplierItem(supplierItem: SupplierItem, onComplete: (Status, String) -> Unit) {
        this.usersCol.firestore.runTransaction {
            it.delete(this.supplierItemsCol.document(supplierItem.id))
            it.update(
                this.valuesCol.document(FieldNames.ValuesCol.SupplierItemsCountDoc.SELF_NAME),
                mapOf(
                    FieldNames.ValuesCol.SupplierItemsCountDoc.VALUE to FieldValue.increment(
                        -1
                    )
                )
            )
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, "Item deleted successfully")
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }

    fun addSupplierPayment(
        supplierPayment: SupplierPayment,
        onComplete: (Status, String) -> Unit
    ) {
        this.supplierPaymentsCol.document().set(supplierPayment)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
                } else {
                    onComplete(Status.FAILED, it.exception?.message.toString())
                }
            }
    }

    fun updateSupplierPayment(
        supplierPayment: SupplierPayment,
        onComplete: (Status, String) -> Unit
    ) {
        this.supplierPaymentsCol.document(supplierPayment.id)
            .update(supplierPayment.toMap()).addOnCompleteListener {
                if (it.isSuccessful) {
                    onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
                } else {
                    onComplete(Status.FAILED, it.exception?.message.toString())
                }
            }
    }

    fun deleteSupplierPayment(
        supplierPayment: SupplierPayment,
        onComplete: (Status, String) -> Unit
    ) {
        this.supplierPaymentsCol.document(supplierPayment.id).delete().addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, "Payment deleted successfully")
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun addOrderedItem(
        orderedItem: OrderedItem,
        onComplete: (Status, String) -> Unit
    ) {
        this.usersCol.firestore.runTransaction {
            it.set(this.orderedItemsCol.document(), orderedItem)
            if (!orderedItem.isReceived) {
                it.update(
                    valuesCol.document(FieldNames.ValuesCol.OrdersToReceiveDoc.SELF_NAME),
                    mapOf(
                        FieldNames.ValuesCol.OrdersToReceiveDoc.VALUE to FieldValue.increment(
                            1
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

    fun updateOrderedItem(
        orderedItem: OrderedItem,
        onComplete: (Status, String) -> Unit
    ) {
        this.usersCol.firestore.runTransaction {
            val prevIsReceived =
                it.get(orderedItemsCol.document(orderedItem.id))
                    .getBoolean(FieldNames.OrderedItemsCol.IS_RECEIVED)
                    ?: false

            if (prevIsReceived == true && orderedItem.isReceived == false) {
                it.update(
                    valuesCol.document(FieldNames.ValuesCol.OrdersToReceiveDoc.SELF_NAME),
                    mapOf(
                        FieldNames.ValuesCol.OrdersToReceiveDoc.VALUE to FieldValue.increment(
                            1
                        )
                    )
                )
            } else if (prevIsReceived == false && orderedItem.isReceived == true) {
                it.update(
                    valuesCol.document(FieldNames.ValuesCol.OrdersToReceiveDoc.SELF_NAME),
                    mapOf(
                        FieldNames.ValuesCol.OrdersToReceiveDoc.VALUE to FieldValue.increment(
                            -1
                        )
                    )
                )
            }
            it.update(orderedItemsCol.document(orderedItem.id), orderedItem.toMap())
        }
    }

    fun deleteOrderedItem(orderedItem: OrderedItem, onComplete: (Status, String) -> Unit) {

        this.usersCol.firestore.runTransaction {
            it.delete(this.orderedItemsCol.document(orderedItem.id))
            if (!orderedItem.isReceived) {
                it.update(
                    valuesCol.document(FieldNames.ValuesCol.OrdersToReceiveDoc.SELF_NAME),
                    mapOf(
                        FieldNames.ValuesCol.OrdersToReceiveDoc.VALUE to FieldValue.increment(
                            -1
                        )
                    )
                )
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, "Ordered item deleted successfully")
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun getNumberOfSuppliers(onComplete: (Status, Number?, String) -> Unit) {
        this.helperRepository.getAnyValueFromValuesCol(
            FieldNames.ValuesCol.SuppliersCountDoc.SELF_NAME, onComplete
        )
    }

    fun getDueAmountToSuppliers(onComplete: (Status, Number?, String) -> Unit) {
        this.helperRepository.getAnyValueFromValuesCol(
            FieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME, onComplete
        )
    }

    fun getOrdersToReceive(onComplete: (Status, Number?, String) -> Unit) {
        this.helperRepository.getAnyValueFromValuesCol(
            FieldNames.ValuesCol.OrdersToReceiveDoc.SELF_NAME, onComplete
        )
    }

    fun getPurchaseAmountsListByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        onComplete: (Status, List<Double>?, String) -> Unit
    ) {
        val query = this.orderedItemsCol
            .whereGreaterThanOrEqualTo(
                FieldNames.OrderedItemsCol.ORDER_TIMESTAMP,
                startTimestamp
            )
            .whereLessThanOrEqualTo(
                FieldNames.OrderedItemsCol.ORDER_TIMESTAMP,
                endTimestamp
            )
            .orderBy(FieldNames.OrderedItemsCol.ORDER_TIMESTAMP, Query.Direction.ASCENDING)

        query.get().addOnCompleteListener {

            if (it.isSuccessful) {
                val resultList = mutableListOf<Double>()

                for (doc in it.result.documents) {
                    if (doc.exists()) {
                        val amount =
                            Converters.numberToDouble(doc.get(OrderedItem::amount.name) as Number)
                        resultList.add(amount)
                    }
                }
                onComplete(
                    Status.SUCCESS,
                    resultList,
                    KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                )
            } else {
                onComplete(Status.FAILED, null, it.exception?.message.toString())
            }
        }
    }

    fun getFrequencyOfOrderedItemsByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        onComplete: (Status, List<Pair<String, Int>>?, String) -> Unit
    ) {
        val query = this.orderedItemsCol
            .whereGreaterThanOrEqualTo(
                FieldNames.OrderedItemsCol.ORDER_TIMESTAMP,
                startTimestamp
            )
            .whereLessThanOrEqualTo(
                FieldNames.OrderedItemsCol.ORDER_TIMESTAMP,
                endTimestamp
            )
            .orderBy(FieldNames.OrderedItemsCol.ORDER_TIMESTAMP, Query.Direction.ASCENDING)

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val map = hashMapOf<String, Int>()

                for (doc in it.result.documents) {
                    if (doc.exists()) {
                        val itemName = doc.get(OrderedItem::supplierItemName.name) as String
                        map[itemName] = map.getOrDefault(itemName, 0) + 1
                    }
                }
                onComplete(
                    Status.SUCCESS,
                    map.toList(),
                    KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                )
            } else {
                onComplete(Status.FAILED, null, it.exception?.message.toString())
            }
        }
    }

    fun getSupplierPaymentsByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        onComplete: (Status, List<SupplierPayment>?, String) -> Unit
    ) {
        val query = this.supplierPaymentsCol
            .whereGreaterThanOrEqualTo(
                FieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP,
                startTimestamp
            )
            .whereLessThanOrEqualTo(
                FieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP,
                endTimestamp
            )
            .orderBy(
                FieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP,
                Query.Direction.ASCENDING
            )

        query.get().addOnCompleteListener {

            if (it.isSuccessful) {

                val resultList = mutableListOf<SupplierPayment>()

                for (doc in it.result.documents) {
                    if (doc.exists()) {

                        val supplierPayment = doc.toObject(SupplierPayment::class.java)!!

                        resultList.add(supplierPayment)
                    }
                }
                onComplete(
                    Status.SUCCESS,
                    resultList,
                    KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                )
            } else {
                onComplete(Status.FAILED, null, it.exception?.message.toString())
            }
        }
    }

    fun getOrderedItemsByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        onComplete: (Status, MutableList<OrderedItem>?, String) -> Unit
    ) {
        val query = this.orderedItemsCol
            .whereGreaterThanOrEqualTo(
                FieldNames.OrderedItemsCol.ORDER_TIMESTAMP,
                startTimestamp
            )
            .whereLessThanOrEqualTo(
                FieldNames.OrderedItemsCol.ORDER_TIMESTAMP,
                endTimestamp
            )
            .orderBy(
                FieldNames.OrderedItemsCol.ORDER_TIMESTAMP,
                Query.Direction.ASCENDING
            )

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val resultList = mutableListOf<OrderedItem>()
                for (doc in it.result.documents) {
                    if (doc.exists()) {
                        val orderedItem = doc.toObject(OrderedItem::class.java)!!
                        resultList.add(orderedItem)
                    }
                }
                onComplete(
                    Status.SUCCESS,
                    resultList,
                    KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                )
            } else {
                onComplete(Status.FAILED, null, it.exception?.message.toString())
            }
        }
    }

    suspend fun getCurrentUserDetails(): Triple<Status, User?, String> =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                currentUserDoc.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val doc = task.result
                        if (doc.exists()) {
                            val user = doc.toObject(User::class.java)
                            continuation.resume(Triple(Status.SUCCESS, user, ""))
                        } else {
                            continuation.resume(
                                Triple(
                                    Status.FAILED,
                                    null,
                                    "Document data is null"
                                )
                            )
                        }
                    } else {
                        val errorMessage = task.exception?.message ?: "Unknown error"
                        continuation.resume(Triple(Status.FAILED, null, errorMessage))
                    }
                }
            }
        }

    fun getPurchaseAmountByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        onComplete: (Status, Number?, String) -> Unit
    ) {
        val query = this.orderedItemsCol
            .whereGreaterThanOrEqualTo(
                FieldNames.OrderedItemsCol.ORDER_TIMESTAMP,
                startTimestamp
            )
            .whereLessThanOrEqualTo(
                FieldNames.OrderedItemsCol.ORDER_TIMESTAMP,
                endTimestamp
            ).aggregate(AggregateField.sum(OrderedItem::amount.name))

        query.get(AggregateSource.SERVER).addOnCompleteListener {
            if (it.isSuccessful) {
                val sum =
                    it.result.get(AggregateField.sum(OrderedItem::amount.name)) as? Number

                if (sum != null) {
                    onComplete(
                        Status.SUCCESS,
                        Converters.numberToDouble(sum),
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                } else {
                    onComplete(Status.FAILED, null, KeysAndMessages.TASK_FAILED_TO_COMPLETE)
                }
            } else {
                onComplete(Status.FAILED, null, it.exception?.message.toString())
            }
        }
    }
}