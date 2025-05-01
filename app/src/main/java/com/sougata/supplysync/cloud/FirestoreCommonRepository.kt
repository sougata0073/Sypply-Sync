package com.sougata.supplysync.cloud

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import com.sougata.supplysync.util.modelslist.DataType
import kotlin.math.ceil
import kotlin.math.floor

class FirestoreCommonRepository {

    private val currentUser = Firebase.auth.currentUser
    private val db = Firebase.firestore
    private val usersCol =
        this.db.collection(FieldNamesRepository.UsersCollection.THIS_COLLECTION_NAME)

    fun getAnyModelsList(
        firebaseCollectionName: String,
        lastDocumentSnapshot: DocumentSnapshot?,
        customSorting: Pair<String, Query.Direction>,
        limit: Long,
        howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {
        if (currentUser == null) {
            onComplete(Status.FAILED, null, null, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val col = this.usersCol.document(this.currentUser.uid).collection(firebaseCollectionName)

        var query = if (lastDocumentSnapshot == null) {
            col.orderBy(customSorting.first, customSorting.second)
        } else {
            col.orderBy(customSorting.first, customSorting.second).startAfter(lastDocumentSnapshot)
        }

        val querySnapshot = if (limit == -1L) {
            query.get()
        } else {
            query.limit(limit).get()
        }

        querySnapshot.addOnCompleteListener {

            if (it.isSuccessful) {

                val modelsList = mutableListOf<Model>()

                for (document in it.result.documents) {

                    val data = document.data

                    if (data != null && document.exists()) {
                        val model: Model = howToConvert(data, document)
                        modelsList.add(model)
                    }
                }

//                    delay(2000)

                if (it.result.documents.isEmpty()) {
                    // When no more data is there
                    onComplete(Status.SUCCESS, modelsList, null, KeysAndMessages.EMPTY_LIST)
                } else {
                    onComplete(
                        Status.SUCCESS,
                        modelsList,
                        it.result.documents.last(),
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                }

            } else {

                onComplete(
                    Status.FAILED,
                    null,
                    lastDocumentSnapshot,
                    it.exception?.message.toString()
                )

            }

        }
    }

    fun searchInAnyModelsList(
        searchField: String,
        searchQuery: String,
        queryDataType: DataType,
        firebaseCollectionName: String,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        howToConvert: (Map<String, Any>, DocumentSnapshot) -> Model,
        onComplete: (Int, MutableList<Model>?, DocumentSnapshot?, String) -> Unit,
    ) {

        if (currentUser == null) {
            onComplete(Status.FAILED, null, null, KeysAndMessages.USER_NOT_FOUND)
            return
        }

        val col = this.usersCol.document(this.currentUser.uid).collection(firebaseCollectionName)

        var query = if (queryDataType == DataType.NUMBER) {
            val lowerBound = floor(searchQuery.toDouble())
            val upperBound = lowerBound + 1

            col.whereGreaterThanOrEqualTo(searchField, lowerBound)
                .whereLessThanOrEqualTo(searchField, upperBound)

        } else {
            col.orderBy(searchField).startAt(searchQuery).endAt(searchQuery + "\uf8ff")
        }.limit(limit)

        if (lastDocumentSnapshot != null) {
            query = query.startAfter(lastDocumentSnapshot)
        }

        query.get().addOnCompleteListener {

            if (it.isSuccessful) {

                val modelsList = mutableListOf<Model>()

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
                } else {
                    onComplete(
                        Status.SUCCESS,
                        modelsList,
                        it.result.documents.last(),
                        KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                }

            } else {
                onComplete(
                    Status.FAILED,
                    null,
                    lastDocumentSnapshot,
                    it.exception?.message.toString()
                )
            }

        }
    }

}