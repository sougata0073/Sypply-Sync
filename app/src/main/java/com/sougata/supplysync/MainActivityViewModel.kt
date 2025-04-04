package com.sougata.supplysync

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivityViewModel : ViewModel() {

    val currentUser = Firebase.auth.currentUser


}