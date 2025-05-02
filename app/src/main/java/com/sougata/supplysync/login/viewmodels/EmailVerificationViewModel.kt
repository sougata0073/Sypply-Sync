package com.sougata.supplysync.login.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sougata.supplysync.remote.AuthenticationRepository
import com.sougata.supplysync.util.Status

class EmailVerificationViewModel : ViewModel() {

    private val authRepository = AuthenticationRepository()

    val emailVerifiedIndicator = MutableLiveData<Pair<Int, String>>()

    val emailAgainSendIndicator = MutableLiveData<Pair<Int, String>>()

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