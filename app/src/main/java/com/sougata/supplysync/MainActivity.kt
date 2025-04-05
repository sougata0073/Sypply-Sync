package com.sougata.supplysync

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.sougata.supplysync.databinding.ActivityMainBinding
import com.sougata.supplysync.login.LoginActivity
import com.sougata.supplysync.util.KeysAndMessages

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: MainActivityViewModel

    override fun onStart() {
        super.onStart()

//        Firebase.auth.addAuthStateListener { auth ->
//            val currentUser = auth.currentUser
//
//            if(currentUser != null) {
//                Log.d("auth", "User is logged in")
//            } else {
//                Log.d("auth", "User is not logged in")
//                startActivity(Intent(this, LoginActivity::class.java))
//                finish()
//            }
//        }


        this.viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        val currentUser = this.viewModel.currentUser

        if (currentUser == null) {
            val bundle = Bundle().apply {
                putString("reason", KeysAndMessages.USER_NOT_FOUND)
            }

            startActivity(Intent(this, LoginActivity::class.java), bundle)
            finish()

        } else if (!currentUser.isEmailVerified) {

            val bundle = Bundle().apply {
                putString("reason", KeysAndMessages.EMAIL_IS_NOT_VERIFIED)
            }

            val intent = Intent(this, LoginActivity::class.java).apply {
                putExtras(bundle)
            }

            startActivity(intent)
            finish()
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        this.binding.bottomNav.isItemActiveIndicatorEnabled = false

        this.binding.lifecycleOwner = this

        this.setUpNavController()

    }

    private fun setUpNavController() {
        val navController =
            (this.supportFragmentManager.findFragmentById(R.id.navHostMain) as NavHostFragment).navController

        this.binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { navController, destination, arguments ->

            when (destination.id) {
                R.id.addEditSupplierFragment, R.id.addEditSupplierPaymentFragment -> {
                    this.binding.bottomNav.visibility = View.GONE
                }
                else -> this.binding.bottomNav.visibility = View.VISIBLE
            }

        }
    }
}