package com.sougata.supplysync.firestore

import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sougata.supplysync.firestore.util.FieldNames
import com.sougata.supplysync.firestore.util.Helper
import com.sougata.supplysync.firestore.util.ModelMaps
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

    private val helper = Helper()
    private val modelMaps = ModelMaps()

    companion object {
        const val TO_ADD = "To add"
        const val TO_UPDATE = "To update"
    }

    fun insertUserToFirestore(user: User, onComplete: (Int, String) -> Unit) {
        
        val userDoc = mapOf(
            FieldNames.UsersCol.NAME to user.name,
            FieldNames.UsersCol.EMAIL to user.email,
            FieldNames.UsersCol.PHONE to user.phone
        )

        usersCol.document(user.uid).set(userDoc)
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    this.createRequiredThings { status, message ->
                        if (status == Status.SUCCESS) {
                            onComplete(Status.SUCCESS, "User successfully added to database")
                        } else if (status == Status.FAILED) {
                            onComplete(Status.FAILED, message)
                        }
                    }

                } else {
                    onComplete(Status.FAILED, it.exception?.message.toString())
                }
            }

    }

    fun getSuppliersList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {

        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.SUPPLIER, map, document)
        }

        this.helper.getAnyModelsList(
            firebaseCollectionName = FieldNames.SuppliersCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.SuppliersCol.TIMESTAMP to Query.Direction.ASCENDING,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )

    }

    fun getSuppliersListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.SUPPLIER, map, document)
        }

        this.helper.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FieldNames.SuppliersCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getSupplierItemsList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {

        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.SUPPLIERS_ITEM, map, document)
        }

        this.helper.getAnyModelsList(
            firebaseCollectionName = FieldNames.SupplierItemsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.SupplierItemsCol.TIMESTAMP to Query.Direction.ASCENDING,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getSupplierItemsListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.SUPPLIERS_ITEM, map, document)
        }

        this.helper.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FieldNames.SupplierItemsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getOrderedItemsList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.ORDERED_ITEM, map, document)
        }

        this.helper.getAnyModelsList(
            firebaseCollectionName = FieldNames.OrderedItemsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.OrderedItemsCol.TIMESTAMP to Query.Direction.ASCENDING,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getOrderedItemsListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.ORDERED_ITEM, map, document)
        }

        this.helper.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FieldNames.OrderedItemsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getSupplierPaymentsList(
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.SUPPLIER_PAYMENT, map, document)
        }

        this.helper.getAnyModelsList(
            firebaseCollectionName = FieldNames.SupplierPaymentsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            customSorting = FieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP to Query.Direction.DESCENDING,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun getSupplierPaymentsListFiltered(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            this.modelMaps.getMappedModel(Model.SUPPLIER_PAYMENT, map, document)
        }

        this.helper.searchInAnyModelsList(
            searchField = searchField,
            searchQuery = searchQuery,
            queryDataType = queryDataType,
            firebaseCollectionName = FieldNames.SupplierPaymentsCol.SELF_NAME,
            lastDocumentSnapshot = lastDocumentSnapshot,
            limit = limit,
            howToConvert = howToConvert,
            onComplete = onComplete
        )
    }

    fun addUpdateSupplier(
        supplier: Supplier, action: String, onComplete: (Int, String) -> Unit
    ) {

        val suppliersCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.SuppliersCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.ValuesCol.SELF_NAME)

        val supplierDoc = mutableMapOf<String, Any>(
            FieldNames.SuppliersCol.NAME to supplier.name,
            FieldNames.SuppliersCol.DUE_AMOUNT to supplier.dueAmount,
            FieldNames.SuppliersCol.PHONE to supplier.phone,
            FieldNames.SuppliersCol.EMAIL to supplier.email,
            FieldNames.SuppliersCol.NOTE to supplier.note,
            FieldNames.SuppliersCol.PAYMENT_DETAILS to supplier.paymentDetails,
            FieldNames.SuppliersCol.PROFILE_IMAGE_URL to supplier.profileImageUrl
        )

        this.usersCol.firestore.runTransaction {
            if (action == TO_ADD) {
                supplierDoc.put(
                    FieldNames.SuppliersCol.TIMESTAMP,
                    FieldValue.serverTimestamp()
                )
                it.set(suppliersCol.document(), supplierDoc)
                it.update(
                    valuesCol.document(FieldNames.ValuesCol.SuppliersCountDoc.SELF_NAME),
                    mapOf(
                        FieldNames.ValuesCol.SuppliersCountDoc.VALUE to FieldValue.increment(
                            1
                        )
                    )
                )
                it.update(
                    valuesCol.document(FieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME),
                    mapOf(
                        FieldNames.ValuesCol.SuppliersDueAmountDoc.VALUE to FieldValue.increment(
                            supplier.dueAmount
                        )
                    )
                )
            } else if (action == TO_UPDATE) {
                var prevTotalDueAmount =
                    it.get(valuesCol.document(FieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME))
                        .getDouble(FieldNames.ValuesCol.SuppliersDueAmountDoc.VALUE)
                        ?: 0.0
                var prevSupplierDueAmount =
                    it.get(suppliersCol.document(supplier.id))
                        .getDouble(FieldNames.SuppliersCol.DUE_AMOUNT) ?: 0.0

                val newTotalDueAmount: Double? =
                    prevTotalDueAmount - prevSupplierDueAmount + supplier.dueAmount

                it.update(suppliersCol.document(supplier.id), supplierDoc)
                it.update(
                    valuesCol.document(FieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME),
                    mapOf(FieldNames.ValuesCol.SuppliersDueAmountDoc.VALUE to newTotalDueAmount)
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

    fun addUpdateSupplierItem(
        supplierItem: SupplierItem, action: String, onComplete: (Int, String) -> Unit
    ) {

        val supplierItemsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FieldNames.SupplierItemsCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.ValuesCol.SELF_NAME)

        val supplierItemDoc = mutableMapOf<String, Any>(
            FieldNames.SupplierItemsCol.NAME to supplierItem.name,
            FieldNames.SupplierItemsCol.PRICE to supplierItem.price,
            FieldNames.SupplierItemsCol.DETAILS to supplierItem.details,
        )

        this.usersCol.firestore.runTransaction {
            if (action == TO_ADD) {
                supplierItemDoc.put(
                    FieldNames.SupplierItemsCol.TIMESTAMP,
                    FieldValue.serverTimestamp()
                )
                it.set(supplierItemsCol.document(), supplierItemDoc)
                it.update(
                    valuesCol.document(FieldNames.ValuesCol.SupplierItemsCountDoc.SELF_NAME),
                    mapOf(
                        FieldNames.ValuesCol.SupplierItemsCountDoc.VALUE to FieldValue.increment(
                            1
                        )
                    )
                )
            } else if (action == TO_UPDATE) {
                it.update(supplierItemsCol.document(supplierItem.id), supplierItemDoc)
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }
    }

    fun addUpdateSupplierPayment(
        supplierPayment: SupplierPayment,
        action: String,
        onComplete: (Int, String) -> Unit
    ) {

        val supplierPaymentsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FieldNames.SupplierPaymentsCol.SELF_NAME)

        val supplierPaymentDoc = mutableMapOf<String, Any>(
            FieldNames.SupplierPaymentsCol.AMOUNT to supplierPayment.amount,
            FieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP to supplierPayment.paymentTimestamp,
            FieldNames.SupplierPaymentsCol.NOTE to supplierPayment.note,
            FieldNames.SupplierPaymentsCol.SUPPLIER_ID to supplierPayment.supplierId,
            FieldNames.SupplierPaymentsCol.SUPPLIER_NAME to supplierPayment.supplierName
        )

        this.usersCol.firestore.runTransaction {
            if (action == TO_ADD) {
                supplierPaymentDoc.put(
                    FieldNames.SupplierPaymentsCol.TIMESTAMP,
                    FieldValue.serverTimestamp()
                )
                it.set(supplierPaymentsCol.document(), supplierPaymentDoc)
            } else if (action == TO_UPDATE) {
                it.update(supplierPaymentsCol.document(supplierPayment.id), supplierPaymentDoc)
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }

    fun addUpdateOrderedItem(
        orderedItem: OrderedItem,
        action: String,
        onComplete: (Int, String) -> Unit
    ) {

        val orderedItemsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FieldNames.OrderedItemsCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.ValuesCol.SELF_NAME)

        val orderedItemDoc = mutableMapOf<String, Any>(
            FieldNames.OrderedItemsCol.ITEM_ID to orderedItem.itemId,
            FieldNames.OrderedItemsCol.ITEM_NAME to orderedItem.itemName,
            FieldNames.OrderedItemsCol.QUANTITY to orderedItem.quantity,
            FieldNames.OrderedItemsCol.AMOUNT to orderedItem.amount,
            FieldNames.OrderedItemsCol.SUPPLIER_ID to orderedItem.supplierId,
            FieldNames.OrderedItemsCol.SUPPLIER_NAME to orderedItem.supplierName,
            FieldNames.OrderedItemsCol.ORDER_TIMESTAMP to orderedItem.orderTimestamp,
            FieldNames.OrderedItemsCol.IS_RECEIVED to orderedItem.isReceived
        )

        this.usersCol.firestore.runTransaction {

            if (action == TO_ADD) {
                orderedItemDoc.put(
                    FieldNames.OrderedItemsCol.TIMESTAMP,
                    FieldValue.serverTimestamp()
                )
                it.set(orderedItemsCol.document(), orderedItemDoc)
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

            } else if (action == TO_UPDATE) {

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
                it.update(orderedItemsCol.document(orderedItem.id), orderedItemDoc)

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
    fun getNumberOfSuppliers(onComplete: (Int, Number, String) -> Unit) {

        this.helper.getAnyValueFromValuesCol(
            FieldNames.ValuesCol.SuppliersCountDoc.SELF_NAME,
            FirestoreFieldDataType.NUMBER,
            onComplete as (Int, Any, String) -> Unit
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun getDueAmountToSuppliers(onComplete: (Int, Number, String) -> Unit) {

        this.helper.getAnyValueFromValuesCol(
            FieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME,
            FirestoreFieldDataType.NUMBER,
            onComplete as (Int, Any, String) -> Unit
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun getOrdersToReceive(onComplete: (Int, Number, String) -> Unit) {

        this.helper.getAnyValueFromValuesCol(
            FieldNames.ValuesCol.OrdersToReceiveDoc.SELF_NAME,
            FirestoreFieldDataType.NUMBER,
            onComplete as (Int, Any, String) -> Unit
        )
    }

    fun deleteSupplier(supplier: Supplier, onComplete: (Int, String) -> Unit) {

        val suppliersCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.SuppliersCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.ValuesCol.SELF_NAME)

        this.usersCol.firestore.runTransaction {
            it.delete(suppliersCol.document(supplier.id))
            it.update(
                valuesCol.document(FieldNames.ValuesCol.SuppliersCountDoc.SELF_NAME),
                mapOf(
                    FieldNames.ValuesCol.SuppliersCountDoc.VALUE to FieldValue.increment(
                        -1
                    )
                )
            )
            it.update(
                valuesCol.document(FieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME),
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

    fun deleteSupplierItem(supplierItem: SupplierItem, onComplete: (Int, String) -> Unit) {

        val supplierItemsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FieldNames.SupplierItemsCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.ValuesCol.SELF_NAME)

        this.usersCol.firestore.runTransaction {
            it.delete(supplierItemsCol.document(supplierItem.id))
            it.update(
                valuesCol.document(FieldNames.ValuesCol.SupplierItemsCountDoc.SELF_NAME),
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

    fun deleteSupplierPayment(supplierPayment: SupplierPayment, onComplete: (Int, String) -> Unit) {

        val supplierPaymentsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FieldNames.SupplierPaymentsCol.SELF_NAME)

        this.usersCol.firestore.runTransaction {
            it.delete(supplierPaymentsCol.document(supplierPayment.id))
        }.addOnCompleteListener {

            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, "Payment deleted successfully")
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }

        }

    }

    fun deleteOrderedItem(orderedItem: OrderedItem, onComplete: (Int, String) -> Unit) {

        val orderedItemsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FieldNames.OrderedItemsCol.SELF_NAME)
        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.ValuesCol.SELF_NAME)

        this.usersCol.firestore.runTransaction {
            it.delete(orderedItemsCol.document(orderedItem.id))
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

    fun getPurchaseAmountByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        onComplete: (Int, MutableList<Double>, String) -> Unit
    ) {

        val orderedItemsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FieldNames.OrderedItemsCol.SELF_NAME)

        val query = orderedItemsCol
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
                    val data = doc.data

                    if (doc.exists() && data != null) {
                        val amount =
                            Converters.numberToDouble(data[FieldNames.OrderedItemsCol.AMOUNT] as Number)
                        resultList.add(amount)
                    }
                }

                onComplete(
                    Status.SUCCESS,
                    resultList,
                    KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                )


            } else {
                onComplete(Status.FAILED, mutableListOf(), it.exception?.message.toString())
            }

        }
    }

    fun getFrequencyOfOrderedItemsByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        onComplete: (Int, MutableList<Pair<String, Int>>, String) -> Unit
    ) {

        val orderedItemsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FieldNames.OrderedItemsCol.SELF_NAME)

        val query = orderedItemsCol
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
                val resultList = mutableListOf<Pair<String, Int>>()


                for (doc in it.result.documents) {
                    val data = doc.data
                    if (doc.exists() && data != null) {
                        val itemName =
                            data[FieldNames.OrderedItemsCol.ITEM_NAME] as String
                        map[itemName] = map.getOrDefault(itemName, 0) + 1
                    }
                }
                for (item in map) {
                    resultList.add(Pair(item.key, item.value))
                }

                onComplete(
                    Status.SUCCESS,
                    resultList,
                    KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                )


            } else {
                onComplete(Status.FAILED, mutableListOf(), it.exception?.message.toString())
            }

        }
    }

    fun getSupplierPaymentsByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        onComplete: (Int, MutableList<SupplierPayment>, String) -> Unit
    ) {

        val supplierPaymentsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FieldNames.SupplierPaymentsCol.SELF_NAME)

        val query = supplierPaymentsCol
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
                    val data = doc.data

                    if (doc.exists() && data != null) {
                        val supplierPayment = SupplierPayment(
                            amount = Converters.numberToDouble(data[FieldNames.SupplierPaymentsCol.AMOUNT] as Number),
                            paymentTimestamp = data[FieldNames.SupplierPaymentsCol.PAYMENT_TIMESTAMP] as Timestamp,
                            note = data[FieldNames.SupplierPaymentsCol.NOTE] as String,
                            supplierId = data[FieldNames.SupplierPaymentsCol.SUPPLIER_ID] as String,
                            supplierName = data[FieldNames.SupplierPaymentsCol.SUPPLIER_NAME] as String
                        ).apply {
                            id = doc.id
                            timestamp =
                                data[FieldNames.SupplierPaymentsCol.TIMESTAMP] as Timestamp
                        }
                        resultList.add(supplierPayment)
                    }
                }

                onComplete(
                    Status.SUCCESS,
                    resultList,
                    KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                )

            } else {
                onComplete(Status.FAILED, mutableListOf(), it.exception?.message.toString())
            }

        }
    }

    fun getOrderedItemsByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        onComplete: (Int, MutableList<OrderedItem>, String) -> Unit
    ) {
        val orderedItemsCol =
            this.usersCol.document(this.currentUser.uid)
                .collection(FieldNames.OrderedItemsCol.SELF_NAME)

        val query = orderedItemsCol
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
                    val data = doc.data

                    if (doc.exists() && data != null) {
                        val orderedItem = OrderedItem(
                            itemId = data[FieldNames.OrderedItemsCol.ITEM_ID] as String,
                            itemName = data[FieldNames.OrderedItemsCol.ITEM_NAME] as String,
                            quantity = Converters.numberToInt(data[FieldNames.OrderedItemsCol.QUANTITY] as Number),
                            amount = Converters.numberToDouble(data[FieldNames.OrderedItemsCol.AMOUNT] as Number),
                            supplierId = data[FieldNames.OrderedItemsCol.SUPPLIER_ID] as String,
                            supplierName = data[FieldNames.OrderedItemsCol.SUPPLIER_NAME] as String,
                            orderTimestamp = data[FieldNames.OrderedItemsCol.ORDER_TIMESTAMP] as Timestamp,
                            isReceived = data[FieldNames.OrderedItemsCol.IS_RECEIVED] as Boolean
                        ).apply {
                            id = doc.id
                            timestamp =
                                data[FieldNames.OrderedItemsCol.TIMESTAMP] as Timestamp
                        }
                        resultList.add(orderedItem)
                    }
                }

                onComplete(
                    Status.SUCCESS,
                    resultList,
                    KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                )

            } else {
                onComplete(Status.FAILED, mutableListOf(), it.exception?.message.toString())
            }
        }
    }

    suspend fun getCurrentUserDetails(): Triple<Int, User?, String> = withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->
            usersCol.document(currentUser.uid).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val doc = task.result
                    val map = doc.data
                    if (map != null) {
                        val user = User(
                            map[FieldNames.UsersCol.NAME] as String,
                            map[FieldNames.UsersCol.EMAIL] as String,
                            map[FieldNames.UsersCol.PHONE] as String,
                            doc.id
                        )
                        continuation.resume(Triple(Status.SUCCESS, user, ""))
                    } else {
                        continuation.resume(Triple(Status.FAILED, null, "Document data is null"))
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Unknown error"
                    continuation.resume(Triple(Status.FAILED, null, errorMessage))
                }
            }
        }
    }


    private fun createRequiredThings(onComplete: (Int, String) -> Unit) {

        val valuesCol = this.usersCol.document(this.currentUser.uid)
            .collection(FieldNames.ValuesCol.SELF_NAME)

        this.usersCol.firestore.runTransaction {

            // Add value fields here
            val map = mapOf(FieldNames.Commons.VALUE to 0)

            it.set(
                valuesCol.document(FieldNames.ValuesCol.SuppliersCountDoc.SELF_NAME),
                map
            )
            it.set(
                valuesCol.document(FieldNames.ValuesCol.SuppliersDueAmountDoc.SELF_NAME),
                map
            )
            it.set(
                valuesCol.document(FieldNames.ValuesCol.SupplierItemsCountDoc.SELF_NAME),
                map
            )
            it.set(
                valuesCol.document(FieldNames.ValuesCol.OrdersToReceiveDoc.SELF_NAME),
                map
            )

        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }

}