package com.sougata.supplysync.login.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.sougata.supplysync.R
import com.sougata.supplysync.firestore.AuthRepository
import com.sougata.supplysync.util.Status
import com.sougata.supplysync.util.Validations

class LoginViewModel : ViewModel() {

    val email = MutableLiveData("")
    val password = MutableLiveData("")

    val accountLoginIndicator = MutableLiveData<Pair<Int, String>>()

    fun onLoginClick(view: View) {

        var email = this.email.value.toString().trim()
        var password = this.password.value.toString().trim()

        val validations = Validations()

        // Email validation
        if (!validations.validateEmail(email, view)) {
            return
        }

        // Password validation
        if (!validations.validatePassword(password, view)) {
            return
        }

        // When all validation is passed
        this.accountLoginIndicator.postValue(Status.STARTED to "")

        val authRepository = AuthRepository()

        authRepository.loginAccount(email, password) { status, message ->
            this.accountLoginIndicator.postValue(status to message)
        }

    }

    fun onCreateAccountClick(view: View) {
        view.findNavController().navigate(R.id.action_loginFragment_to_createAccountFragment)
    }


}