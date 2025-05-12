package com.sougata.supplysync.firestore.util

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.User
import com.sougata.supplysync.util.FirestoreFieldDataType
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import kotlin.math.floor

class HelperRepository {

    private val currentUser = Firebase.auth.currentUser!!
    private val db = Firebase.firestore
    private val usersCol = this.db.collection(FirestoreNames.Col.USERS)
    private val currentUserDoc = this.usersCol.document(this.currentUser.uid)

    fun <T : Model> getAnyModelsList(
        firebaseCollectionName: String,
        lastDocumentSnapshot: DocumentSnapshot?,
        customSorting: Pair<String, Query.Direction>,
        limit: Long,
        clazz: Class<T>,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit
    ) {

        val col = this.currentUserDoc.collection(firebaseCollectionName)

        var query = if (lastDocumentSnapshot == null) {
            col.orderBy(customSorting.first, customSorting.second)
        } else {
            col.orderBy(customSorting.first, customSorting.second).startAfter(lastDocumentSnapshot)
        }.limit(limit)

        query.get().addOnCompleteListener {

            if (it.isSuccessful) {

                val modelsList = mutableListOf<Model>()

                for (doc in it.result.documents) {

                    if (doc.exists()) {
//                        Log.d("TAG", doc.data.toString())
                        val model: Model = doc.toObject(clazz)!!

                        modelsList.add(model)
                    }
                }

                if (modelsList.isEmpty()) {
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
                    Status.FAILED, null, lastDocumentSnapshot, it.exception?.message.toString()
                )
            }
        }
    }

    fun <T : Model> searchInAnyModelsList(
        searchField: String,
        searchQuery: String,
        queryDataType: FirestoreFieldDataType,
        firebaseCollectionName: String,
        lastDocumentSnapshot: DocumentSnapshot?,
        limit: Long,
        clazz: Class<T>,
        onComplete: (Status, MutableList<Model>?, DocumentSnapshot?, String) -> Unit,
    ) {

        val col = this.usersCol.document(this.currentUser.uid).collection(firebaseCollectionName)

        var query = if (queryDataType == FirestoreFieldDataType.NUMBER) {
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

                    if (document.exists()) {
                        val model: T = document.toObject(clazz)!!
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
                    Status.FAILED, null, lastDocumentSnapshot, it.exception?.message.toString()
                )
            }
        }

    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getAnyValueFromValuesCol(
        documentName: String,
        onComplete: (Status, T?, String) -> Unit
    ) {
        val valuesCol =
            this.usersCol.document(this.currentUser.uid).collection(FirestoreNames.Col.VALUES)

        valuesCol.document(documentName).get().addOnCompleteListener {
            if (it.isSuccessful) {

                val result = it.result[FirestoreNames.ValuesDoc.Fields.VALUE] as? T

                if (result != null) {
                    onComplete(
                        Status.SUCCESS, result, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY
                    )
                } else {
                    onComplete(Status.FAILED, null, KeysAndMessages.EMPTY_LIST)
                }
            } else {
                onComplete(Status.FAILED, null, it.exception?.message.toString())
            }
        }
    }

}