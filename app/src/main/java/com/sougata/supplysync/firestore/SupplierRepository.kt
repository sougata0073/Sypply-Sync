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
import com.sougata.supplysync.firestore.util.FirestoreNames
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
        this.db.collection(FirestoreNames.Col.USERS)
    private val currentUserDoc = this.usersCol.document(this.currentUser.uid)

    private val valuesCol = this.currentUserDoc.collection(FirestoreNames.Col.VALUES)

    private val suppliersCol = this.currentUserDoc.collection(FirestoreNames.Col.SUPPLIERS)
    private val supplierItemsCol =
        this.currentUserDoc.collection(FirestoreNames.Col.SUPPLIER_ITEMS)
    val supplierPaymentsCol =
        this.currentUserDoc.collection(FirestoreNames.Col.SUPPLIER_PAYMENTS)
    val orderedItemsCol =
        this.currentUserDoc.collection(FirestoreNames.Col.ORDERED_ITEMS)

    private val helperRepository = HelperRepository()

    fun getSuppliersList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        this.helperRepository.getAnyModelsList(
            firebaseCollectionName = FirestoreNames.Col.SUPPLIERS,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = Supplier::timestamp.name to Query.Direction.ASCENDING,
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
            firebaseCollectionName = FirestoreNames.Col.SUPPLIERS,
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
            firebaseCollectionName = FirestoreNames.Col.SUPPLIER_ITEMS,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = SupplierItem::timestamp.name to Query.Direction.ASCENDING,
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
            firebaseCollectionName = FirestoreNames.Col.SUPPLIER_ITEMS,
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
            firebaseCollectionName = FirestoreNames.Col.ORDERED_ITEMS,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = OrderedItem::timestamp.name to Query.Direction.ASCENDING,
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
            firebaseCollectionName = FirestoreNames.Col.ORDERED_ITEMS,
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
            firebaseCollectionName = FirestoreNames.Col.SUPPLIER_PAYMENTS,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = SupplierPayment::paymentTimestamp.name to Query.Direction.DESCENDING,
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
            firebaseCollectionName = FirestoreNames.Col.SUPPLIER_PAYMENTS,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            clazz = SupplierPayment::class.java,
            onComplete = onComplete
        )
    }

    fun addSupplier(supplier: Supplier, onComplete: (Status, String) -> Unit) {

        this.usersCol.firestore.runTransaction {
            it.set(this.suppliersCol.document(supplier.id), supplier)
            it.update(
                this.valuesCol.document(FirestoreNames.ValuesDoc.SUPPLIERS_COUNT),
                mapOf(
                    FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
                        1
                    )
                )
            )
            it.update(
                this.valuesCol.document(FirestoreNames.ValuesDoc.SUPPLIERS_DUE_AMOUNT),
                mapOf(
                    FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
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
                it.get(this.valuesCol.document(FirestoreNames.ValuesDoc.SUPPLIERS_DUE_AMOUNT))
                    .getDouble(FirestoreNames.ValuesDoc.Fields.VALUE)
                    ?: 0.0
            var prevSupplierDueAmount =
                it.get(this.suppliersCol.document(supplier.id))
                    .getDouble(Supplier::dueAmount.name) ?: 0.0

            val newTotalDueAmount: Double? =
                prevTotalDueAmount - prevSupplierDueAmount + supplier.dueAmount

            it.update(this.suppliersCol.document(supplier.id), supplier.toMap())
            it.update(
                this.valuesCol.document(FirestoreNames.ValuesDoc.SUPPLIERS_DUE_AMOUNT),
                mapOf(FirestoreNames.ValuesDoc.Fields.VALUE to newTotalDueAmount)
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
                this.valuesCol.document(FirestoreNames.ValuesDoc.SUPPLIERS_COUNT),
                mapOf(
                    FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
                        -1
                    )
                )
            )
            it.update(
                this.valuesCol.document(FirestoreNames.ValuesDoc.SUPPLIERS_DUE_AMOUNT),
                mapOf(
                    FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
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
            it.set(this.supplierItemsCol.document(supplierItem.id), supplierItem)
            it.update(
                this.valuesCol.document(FirestoreNames.ValuesDoc.SUPPLIER_ITEMS_COUNT),
                mapOf(
                    FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
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
                this.valuesCol.document(FirestoreNames.ValuesDoc.SUPPLIER_ITEMS_COUNT),
                mapOf(
                    FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
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
        this.supplierPaymentsCol.document(supplierPayment.id).set(supplierPayment)
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
            it.set(this.orderedItemsCol.document(orderedItem.id), orderedItem)
            if (!orderedItem.received) {
                it.update(
                    valuesCol.document(FirestoreNames.ValuesDoc.ORDERS_TO_RECEIVE),
                    mapOf(
                        FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
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
                    .getBoolean(OrderedItem::received.name)
                    ?: false

            if (prevIsReceived == true && orderedItem.received == false) {
                it.update(
                    valuesCol.document(FirestoreNames.ValuesDoc.ORDERS_TO_RECEIVE),
                    mapOf(
                        FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
                            1
                        )
                    )
                )
            } else if (prevIsReceived == false && orderedItem.received == true) {
                it.update(
                    valuesCol.document(FirestoreNames.ValuesDoc.ORDERS_TO_RECEIVE),
                    mapOf(
                        FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
                            -1
                        )
                    )
                )
            }
            it.update(orderedItemsCol.document(orderedItem.id), orderedItem.toMap())
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun deleteOrderedItem(orderedItem: OrderedItem, onComplete: (Status, String) -> Unit) {

        this.usersCol.firestore.runTransaction {
            it.delete(this.orderedItemsCol.document(orderedItem.id))
            if (!orderedItem.received) {
                it.update(
                    valuesCol.document(FirestoreNames.ValuesDoc.ORDERS_TO_RECEIVE),
                    mapOf(
                        FirestoreNames.ValuesDoc.Fields.VALUE to FieldValue.increment(
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
            FirestoreNames.ValuesDoc.SUPPLIERS_COUNT, onComplete
        )
    }

    fun getDueAmountToSuppliers(onComplete: (Status, Number?, String) -> Unit) {
        this.helperRepository.getAnyValueFromValuesCol(
            FirestoreNames.ValuesDoc.SUPPLIERS_DUE_AMOUNT, onComplete
        )
    }

    fun getOrdersToReceive(onComplete: (Status, Number?, String) -> Unit) {
        this.helperRepository.getAnyValueFromValuesCol(
            FirestoreNames.ValuesDoc.ORDERS_TO_RECEIVE, onComplete
        )
    }

    fun getPurchaseAmountListByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        onComplete: (Status, List<Double>?, String) -> Unit
    ) {
        val query = this.orderedItemsCol
            .whereGreaterThanOrEqualTo(
                OrderedItem::orderTimestamp.name,
                startTimestamp
            )
            .whereLessThanOrEqualTo(
                OrderedItem::orderTimestamp.name,
                endTimestamp
            )
            .orderBy(OrderedItem::orderTimestamp.name, Query.Direction.ASCENDING)

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
                OrderedItem::orderTimestamp.name,
                startTimestamp
            )
            .whereLessThanOrEqualTo(
                OrderedItem::orderTimestamp.name,
                endTimestamp
            )
            .orderBy(OrderedItem::orderTimestamp.name, Query.Direction.ASCENDING)

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
                SupplierPayment::paymentTimestamp.name,
                startTimestamp
            )
            .whereLessThanOrEqualTo(
                SupplierPayment::paymentTimestamp.name,
                endTimestamp
            )
            .orderBy(
                SupplierPayment::paymentTimestamp.name,
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
                OrderedItem::orderTimestamp.name,
                startTimestamp
            )
            .whereLessThanOrEqualTo(
                OrderedItem::orderTimestamp.name,
                endTimestamp
            )
            .orderBy(
                OrderedItem::orderTimestamp.name,
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
                OrderedItem::orderTimestamp.name,
                startTimestamp
            )
            .whereLessThanOrEqualTo(
                OrderedItem::orderTimestamp.name,
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