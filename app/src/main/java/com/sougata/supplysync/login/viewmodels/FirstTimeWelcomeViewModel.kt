package com.sougata.supplysync.login.viewmodels

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.sougata.supplysync.R

class FirstTimeWelcomeViewModel: ViewModel() {

    fun onCreateAccountClick(view: View) {
        view.findNavController().navigate(R.id.action_firstTimeWelcomeFragment_to_createAccountFragment)
    }

    fun onLoginClick(view: View) {
        view.findNavController().navigate(R.id.action_firstTimeWelcomeFragment_to_loginFragment)
    }
}