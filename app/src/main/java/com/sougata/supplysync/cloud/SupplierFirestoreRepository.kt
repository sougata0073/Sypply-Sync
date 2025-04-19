package com.sougata.supplysync.cloud

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.util.nextAlphanumericString
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.models.Supplier
import com.sougata.supplysync.models.SupplierItem
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.models.User
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

class SupplierFirestoreRepository {

    private val currentUser = Firebase.auth.currentUser
    private val db = Firebase.firestore
    private val usersCol = this.db.collection("users")

    companion object {
        const val TO_ADD = "To add"
        const val TO_UPDATE = "To update"
    }

//    init {
//        for (i in 1..100) {
//            val rnd = Random(System.currentTimeMillis())
//            val orderedItem = SupplierPayment(
//                rnd.nextDouble() * 100000,
//                Timestamp.now(),
//                rnd.nextAlphanumericString(10),
//                rnd.nextAlphanumericString(10),
//                rnd.nextAlphanumericString(10)
//            ).apply { id = "" }
//            addUpdateSupplierPayment(orderedItem, TO_ADD) { a, b -> }
//        }
//    }

    fun insertUserToFirestore(user: User, onComplete: (Int, String) -> Unit) {

        val userDoc = mapOf(
            "name" to user.name,
            "email" to user.email,
            "phone" to user.phone
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

    fun getSupplierItemsList(
        coroutineScope: CoroutineScope, lastDocumentSnapshot: DocumentSnapshot?, limit: Long,
        onComplete: (Int, MutableList<Model>, DocumentSnapshot?, String) -> Unit
    ) {

        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            SupplierItem(
                name = map["name"] as String,
                price = Converters.numberToDouble(map["price"] as Number),
                details = map["details"] as String
            ).apply {
                id = document.id
                timestamp = map["timestamp"] as Timestamp
            }
        }

        return getAnyModelsList(
            "supplier_items",
            coroutineScope,
            lastDocumentSnapshot,
            "timestamp" to Query.Direction.ASCENDING,
            limit,
            howToConvert,
            onComplete
        )
    }

    fun getSuppliersList(
        coroutineScope: CoroutineScope, lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        onComplete: (Int, MutableList<Model>, DocumentSnapshot?, String) -> Unit
    ) {


        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            Supplier(
                name = map["name"] as String,
                dueAmount = Converters.numberToDouble(map["due_amount"] as Number),
                phone = map["phone"] as String,
                email = map["email"] as String,
                note = map["note"] as String,
                paymentDetails = map["payment_details"] as String,
                profileImageUrl = map["profile_image_url"] as String
            ).apply {
                id = document.id
                timestamp = map["timestamp"] as Timestamp
            }
        }

        return getAnyModelsList(
            "suppliers",
            coroutineScope,
            lastDocumentSnapshot,
            "timestamp" to Query.Direction.ASCENDING,
            limit,
            howToConvert,
            onComplete
        )

    }

    fun getOrderedItemsList(
        coroutineScope: CoroutineScope, lastDocumentSnapshot: DocumentSnapshot?, limit: Long,
        onComplete: (Int, MutableList<Model>, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            OrderedItem(
                itemId = map["item_id"] as String,
                itemName = map["item_name"] as String,
                quantity = Converters.numberToInt(map["quantity"] as Number),
                amount = Converters.numberToDouble(map["amount"] as Number),
                supplierId = map["supplier_id"] as String,
                supplierName = map["supplier_name"] as String,
                orderTimestamp = map["order_timestamp"] as Timestamp,
                isReceived = map["is_received"] as Boolean
            ).apply {
                id = document.id
                timestamp = map["timestamp"] as Timestamp
            }
        }

        return getAnyModelsList(
            "ordered_items",
            coroutineScope,
            lastDocumentSnapshot,
            "timestamp" to Query.Direction.ASCENDING,
            limit,
            howToConvert,
            onComplete
        )
    }

    fun getSupplierPaymentsList(
        coroutineScope: CoroutineScope, lastDocumentSnapshot: DocumentSnapshot?, limit: Long,
        onComplete: (Int, MutableList<Model>, DocumentSnapshot?, String) -> Unit
    ) {
        val howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model = { map, document ->
            SupplierPayment(
                amount = Converters.numberToDouble(map["amount"] as Number),
                paymentTimestamp = map["payment_timestamp"] as Timestamp,
                note = map["note"] as String,
                supplierId = map["supplier_id"] as String,
                supplierName = map["supplier_name"] as String
            ).apply {
                id = document.id
                timestamp = map["timestamp"] as Timestamp
            }
        }

        return getAnyModelsList(
            "supplier_payments",
            coroutineScope,
            lastDocumentSnapshot,
            "timestamp" to Query.Direction.ASCENDING,
            limit,
            howToConvert,
            onComplete
        )
    }

    fun addUpdateSupplier(
        supplier: Supplier, action: String, onComplete: (Int, String) -> Unit
    ) {

        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val suppliersCol = this.usersCol.document(this.currentUser.uid).collection("suppliers")
        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

        val supplierDoc = mutableMapOf<String, Any>(
            "name" to supplier.name,
            "due_amount" to supplier.dueAmount,
            "phone" to supplier.phone,
            "email" to supplier.email,
            "note" to supplier.note,
            "payment_details" to supplier.paymentDetails,
            "profile_image_url" to supplier.profileImageUrl
        )

        this.usersCol.firestore.runTransaction {
            if (action == TO_ADD) {
                supplierDoc.put("timestamp", FieldValue.serverTimestamp())
                it.set(suppliersCol.document(), supplierDoc)
                it.update(
                    valuesCol.document("suppliers_count"),
                    mapOf("value" to FieldValue.increment(1))
                )
                it.update(
                    valuesCol.document("suppliers_due_amount"),
                    mapOf("value" to FieldValue.increment(supplier.dueAmount))
                )
            } else if (action == TO_UPDATE) {
                var prevTotalDueAmount =
                    it.get(valuesCol.document("suppliers_due_amount")).getDouble("value") ?: 0.0
                var prevSupplierDueAmount =
                    it.get(suppliersCol.document(supplier.id)).getDouble("due_amount") ?: 0.0

                val newTotalDueAmount: Double? =
                    prevTotalDueAmount - prevSupplierDueAmount + supplier.dueAmount

                it.update(suppliersCol.document(supplier.id), supplierDoc)
                it.update(
                    valuesCol.document("suppliers_due_amount"),
                    mapOf("value" to newTotalDueAmount)
                )
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
//                Log.d("list", "Supplier added successfully")
            } else {
//                Log.d("list", it.exception?.message.toString())
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }

    fun addUpdateSupplierItem(
        supplierItem: SupplierItem, action: String, onComplete: (Int, String) -> Unit
    ) {
        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val supplierItemsCol =
            this.usersCol.document(this.currentUser.uid).collection("supplier_items")
        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

        val supplierItemDoc = mutableMapOf<String, Any>(
            "name" to supplierItem.name,
            "price" to supplierItem.price,
            "details" to supplierItem.details,
        )

        this.usersCol.firestore.runTransaction {
            if (action == TO_ADD) {
                supplierItemDoc.put("timestamp", FieldValue.serverTimestamp())
                it.set(supplierItemsCol.document(), supplierItemDoc)
                it.update(
                    valuesCol.document("supplier_items_count"),
                    mapOf("value" to FieldValue.increment(1))
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

        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val supplierPaymentsCol =
            this.usersCol.document(this.currentUser.uid).collection("supplier_payments")

        val supplierPaymentDoc = mutableMapOf<String, Any>(
            "amount" to supplierPayment.amount,
            "payment_timestamp" to supplierPayment.paymentTimestamp,
            "note" to supplierPayment.note,
            "supplier_id" to supplierPayment.supplierId,
            "supplier_name" to supplierPayment.supplierName
        )

        this.usersCol.firestore.runTransaction {
            if (action == TO_ADD) {
                supplierPaymentDoc.put("timestamp", FieldValue.serverTimestamp())
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
        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val orderedItemsCol =
            this.usersCol.document(this.currentUser.uid).collection("ordered_items")
        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

        val orderedItemDoc = mutableMapOf<String, Any>(
            "item_id" to orderedItem.itemId,
            "item_name" to orderedItem.itemName,
            "quantity" to orderedItem.quantity,
            "amount" to orderedItem.amount,
            "supplier_id" to orderedItem.supplierId,
            "supplier_name" to orderedItem.supplierName,
            "order_timestamp" to orderedItem.orderTimestamp,
            "is_received" to orderedItem.isReceived
        )

        this.usersCol.firestore.runTransaction {

            if (action == TO_ADD) {
                orderedItemDoc.put("timestamp", FieldValue.serverTimestamp())
                it.set(orderedItemsCol.document(), orderedItemDoc)
                if (!orderedItem.isReceived) {
                    it.update(
                        valuesCol.document("orders_to_receive"),
                        mapOf("value" to FieldValue.increment(1))
                    )
                }

            } else if (action == TO_UPDATE) {

                val prevIsReceived =
                    it.get(orderedItemsCol.document(orderedItem.id)).getBoolean("is_received")
                        ?: false

                if (prevIsReceived == true && orderedItem.isReceived == false) {
                    it.update(
                        valuesCol.document("orders_to_receive"),
                        mapOf("value" to FieldValue.increment(1))
                    )
                } else if (prevIsReceived == false && orderedItem.isReceived == true) {
                    it.update(
                        valuesCol.document("orders_to_receive"),
                        mapOf("value" to FieldValue.increment(-1))
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

    fun getNumberOfSuppliers(onComplete: (Int, Int, String) -> Unit) {

        if (currentUser == null) {
            onComplete(Status.FAILED, 0, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

        valuesCol.document("suppliers_count").get().addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(
                    Status.SUCCESS,
                    Converters.numberToInt(it.result["value"] as Number),
                    KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                )
            } else {
                onComplete(Status.FAILED, 0, it.exception?.message.toString())
            }
        }
    }

    fun getDueAmountToSuppliers(onComplete: (Int, Double, String) -> Unit) {
        if (currentUser == null) {
            onComplete(Status.FAILED, 0.0, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

        valuesCol.document("suppliers_due_amount").get().addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(
                    Status.SUCCESS,
                    Converters.numberToDouble(it.result["value"] as Number),
                    KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                )
            } else {
                onComplete(Status.FAILED, 0.0, it.exception?.message.toString())
            }
        }
    }

    fun getOrdersToReceive(onComplete: (Int, Int, String) -> Unit) {
        if (currentUser == null) {
            onComplete(Status.FAILED, 0, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

        valuesCol.document("orders_to_receive").get().addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(
                    Status.SUCCESS,
                    Converters.numberToInt(it.result["value"] as Number),
                    KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                )
            } else {
                onComplete(Status.FAILED, 0, it.exception?.message.toString())
            }
        }
    }

    fun deleteSupplier(supplier: Supplier, onComplete: (Int, String) -> Unit) {

        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val suppliersCol = this.usersCol.document(this.currentUser.uid).collection("suppliers")
        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

        this.usersCol.firestore.runTransaction {
            it.delete(suppliersCol.document(supplier.id))
            it.update(
                valuesCol.document("suppliers_count"),
                mapOf("value" to FieldValue.increment(-1))
            )
            it.update(
                valuesCol.document("suppliers_due_amount"),
                mapOf("value" to FieldValue.increment(-supplier.dueAmount))
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
        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val supplierItemsCol =
            this.usersCol.document(this.currentUser.uid).collection("supplier_items")
        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

        this.usersCol.firestore.runTransaction {
            it.delete(supplierItemsCol.document(supplierItem.id))
            it.update(
                valuesCol.document("supplier_items_count"),
                mapOf("value" to FieldValue.increment(-1))
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
        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val supplierPaymentsCol =
            this.usersCol.document(this.currentUser.uid).collection("supplier_payments")

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
        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val orderedItemsCol =
            this.usersCol.document(this.currentUser.uid).collection("ordered_items")
        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

        this.usersCol.firestore.runTransaction {
            it.delete(orderedItemsCol.document(orderedItem.id))
            if (!orderedItem.isReceived) {
                it.update(
                    valuesCol.document("orders_to_receive"),
                    mapOf("value" to FieldValue.increment(-1))
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
        coroutineScope: CoroutineScope,
        onComplete: (Int, MutableList<Double>, String) -> Unit
    ) {

//        Log.d("api", "getPurchaseAmountByRange called")

        if (currentUser == null) {
            onComplete(Status.FAILED, mutableListOf(), KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val orderedItemsCol =
            this.usersCol.document(this.currentUser.uid).collection("ordered_items")

        val query = orderedItemsCol
            .whereGreaterThanOrEqualTo("order_timestamp", startTimestamp)
            .whereLessThanOrEqualTo("order_timestamp", endTimestamp)
            .orderBy("order_timestamp", Query.Direction.ASCENDING)

        query.get().addOnCompleteListener {

            if (it.isSuccessful) {

                val resultList = mutableListOf<Double>()

                coroutineScope.launch(Dispatchers.IO) {

                    for (doc in it.result.documents) {
                        val data = doc.data

                        if (doc.exists() && data != null) {
                            val amount = Converters.numberToDouble(data["amount"] as Number)
                            resultList.add(amount)
                        }
                    }

                    onComplete(
                        Status.SUCCESS,
                        resultList,
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                }

            } else {
                onComplete(Status.FAILED, mutableListOf(), it.exception?.message.toString())
            }

        }
    }

    fun getFrequencyOfOrderedItemsByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        coroutineScope: CoroutineScope,
        onComplete: (Int, MutableList<Pair<String, Int>>, String) -> Unit
    ) {
        if (currentUser == null) {
            onComplete(Status.FAILED, mutableListOf(), KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val orderedItemsCol =
            this.usersCol.document(this.currentUser.uid).collection("ordered_items")

        val query = orderedItemsCol
            .whereGreaterThanOrEqualTo("order_timestamp", startTimestamp)
            .whereLessThanOrEqualTo("order_timestamp", endTimestamp)
            .orderBy("order_timestamp", Query.Direction.ASCENDING)

        query.get().addOnCompleteListener {

            if (it.isSuccessful) {

                val map = hashMapOf<String, Int>()
                val resultList = mutableListOf<Pair<String, Int>>()

                coroutineScope.launch(Dispatchers.IO) {

                    for (doc in it.result.documents) {
                        val data = doc.data
                        if (doc.exists() && data != null) {
                            val itemName = data["item_name"] as String
                            map[itemName] = map.getOrDefault(itemName, 0) + 1
                        }
                    }
                    for (item in map) {
                        resultList.add(Pair(item.key, item.value))
                    }
                    Log.d("item", resultList.toString())
                    onComplete(
                        Status.SUCCESS,
                        resultList,
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                }

            } else {
                onComplete(Status.FAILED, mutableListOf(), it.exception?.message.toString())
            }

        }
    }

    fun getSupplierPaymentsByRange(
        startTimestamp: Timestamp,
        endTimestamp: Timestamp,
        coroutineScope: CoroutineScope,
        onComplete: (Int, MutableList<SupplierPayment>, String) -> Unit
    ) {
        if (currentUser == null) {
            onComplete(Status.FAILED, mutableListOf(), KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val supplierPaymentsCol =
            this.usersCol.document(this.currentUser.uid).collection("supplier_payments")

        val query = supplierPaymentsCol
            .whereGreaterThanOrEqualTo("payment_timestamp", startTimestamp)
            .whereLessThanOrEqualTo("payment_timestamp", endTimestamp)
            .orderBy("payment_timestamp", Query.Direction.ASCENDING)

        query.get().addOnCompleteListener {

            if (it.isSuccessful) {

                val resultList = mutableListOf<SupplierPayment>()

                coroutineScope.launch(Dispatchers.IO) {

                    for (doc in it.result.documents) {
                        val data = doc.data

                        if (doc.exists() && data != null) {
                            val supplierPayment = SupplierPayment(
                                amount = Converters.numberToDouble(data["amount"] as Number),
                                paymentTimestamp = data["payment_timestamp"] as Timestamp,
                                note = data["note"] as String,
                                supplierId = data["supplier_id"] as String,
                                supplierName = data["supplier_name"] as String
                            ).apply {
                                id = doc.id
                                timestamp = data["timestamp"] as Timestamp
                            }
                            resultList.add(supplierPayment)
                        }
                    }

                    onComplete(
                        Status.SUCCESS,
                        resultList,
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                }

            } else {
                onComplete(Status.FAILED, mutableListOf(), it.exception?.message.toString())
            }

        }
    }

    fun getCurrentUserDetails(onComplete: (Int, User?, String) -> Unit) {
        if (currentUser == null) {
            onComplete(Status.FAILED, null, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        this.usersCol.document(this.currentUser.uid).get().addOnCompleteListener {

            if (it.isSuccessful) {
                val doc = it.result
                val map = doc.data
                if (map != null) {
                    val user = User(
                        map["name"] as String,
                        map["email"] as String,
                        map["phone"] as String,
                        doc.id
                    )
                    onComplete(Status.SUCCESS, user, it.exception?.message.toString())
                }
            } else {
                onComplete(Status.FAILED, null, it.exception?.message.toString())
            }

        }
    }


    private fun createRequiredThings(onComplete: (Int, String) -> Unit) {

        if (currentUser == null) {
            onComplete(Status.FAILED, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        // Create 'values' sub collection
        val valuesCol = this.usersCol.document(this.currentUser.uid).collection("values")

        this.usersCol.firestore.runTransaction {

            // Add value fields here
            val map = mapOf("value" to 0)

            it.set(valuesCol.document("suppliers_count"), map)
            it.set(valuesCol.document("suppliers_due_amount"), map)
            it.set(valuesCol.document("supplier_items_count"), map)
            it.set(valuesCol.document("orders_to_receive"), map)

        }.addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)
            } else {
                onComplete(Status.FAILED, it.exception?.message.toString())
            }
        }

    }

    private fun getAnyModelsList(
        firebaseCollectionName: String,
        coroutineScope: CoroutineScope,
        lastDocumentSnapshot: DocumentSnapshot?,
        customSorting: Pair<String, Query.Direction>,
        limit: Long,
        howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model,
        onComplete: (Int, MutableList<Model>, DocumentSnapshot?, String) -> Unit,
    ) {
        if (currentUser == null) {
            onComplete(Status.FAILED, mutableListOf(), null, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val col = this.usersCol.document(this.currentUser.uid).collection(firebaseCollectionName)

        var query = if (lastDocumentSnapshot == null) {
            col.orderBy(customSorting.first, customSorting.second)
        } else {
            col.orderBy(customSorting.first, customSorting.second)
                .startAfter(lastDocumentSnapshot)
        }

        val querySnapshot = if (limit == -1L) {
            query.get()
        } else {
            query.limit(limit).get()
        }

        querySnapshot.addOnCompleteListener {

            if (it.isSuccessful) {

                val modelsList = mutableListOf<Model>()

                coroutineScope.launch(Dispatchers.IO) {

                    for (document in it.result.documents) {

                        val data = document.data

                        if (data != null && document.exists()) {
                            val model: Model = howToConvert(data, document)
                            modelsList.add(model)
                        }
                    }

                    if (it.result.documents.isEmpty()) {
                        // When no more data is there
                        onComplete(Status.SUCCESS, modelsList, null, KeysAndMessages.EMPTY_LIST)
//                        Log.d("doc", "empty")
                    } else {
                        onComplete(
                            Status.SUCCESS,
                            modelsList,
                            it.result.documents.last(),
                            KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                        )
//                        Log.d("doc", it.result.documents.last().toString())
                    }
                }

            } else {

                onComplete(Status.FAILED, mutableListOf(), null, it.exception?.message.toString())

            }

        }
    }

}