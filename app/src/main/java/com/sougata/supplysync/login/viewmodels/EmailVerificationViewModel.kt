package com.sougata.supplysync.login.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sougata.supplysync.firestore.AuthRepository
import com.sougata.supplysync.util.Status

class EmailVerificationViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    val emailVerifiedIndicator = MutableLiveData<Pair<Status, String>>()

    val emailAgainSendIndicator = MutableLiveData<Pair<Status, String>>()

    fun onVerifiedClick(view: View) {

        this.emailVerifiedIndicator.postValue(Status.STARTED to "")

        this.authRepository.verifyUsersEmail { status, message ->
            this.emailVerifiedIndicator.postValue(status to message)
        }

    }

    fun onSendLinkAgainClick(view: View) {
        this.emailAgainSendIndicator.postValue(Status.STARTED to "")

        this.authRepository.sendEmailVerificationLink { status, message ->
            this.emailAgainSendIndicator.postValue(status to message)
        }

    }

}