package com.sougata.supplysync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.sougata.supplysync.databinding.ActivityMainBinding
import com.sougata.supplysync.login.LoginActivity
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.ViewAnimator

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainActivityViewModel

    private lateinit var navController: NavController

    private var isBottomNavHidden = false

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
                putString(KeysAndMessages.REASON, KeysAndMessages.USER_NOT_FOUND)
            }

            startActivity(Intent(this, LoginActivity::class.java), bundle)
            finishAffinity()

        } else if (!currentUser.isEmailVerified) {

            val bundle = Bundle().apply {
                putString(KeysAndMessages.REASON, KeysAndMessages.EMAIL_IS_NOT_VERIFIED)
            }

            val intent = Intent(this, LoginActivity::class.java).apply {
                putExtras(bundle)
            }

            startActivity(intent)
            finishAffinity()
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        this._binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        this.binding.lifecycleOwner = this

        this.setupBottomNav()
    }

    private fun setupBottomNav() {

        this.navController =
            (this.supportFragmentManager.findFragmentById(R.id.navHostMain) as NavHostFragment).navController

        val popupMenu = PopupMenu(this, this.binding.bottomNav)
        popupMenu.inflate(R.menu.bottom_nav_menu)
        val menu = popupMenu.menu
        this.binding.bottomNav.setupWithNavController(menu, this.navController)

        val viewAnimator = ViewAnimator(this.binding.bottomNav)

        this.navController.addOnDestinationChangedListener { navController, destination, arguments ->

            when (destination.id) {

                R.id.homeFragment, R.id.staffsHomeFragment,
                R.id.customersHomeFragment, R.id.suppliersHomeFragment -> {
                    if (this.isBottomNavHidden) {
                        viewAnimator.slideUpFade()
                        this.isBottomNavHidden = false
                    }
                }

                else -> {
                    if (!this.isBottomNavHidden) {
                        viewAnimator.slideDownFade()
                        this.isBottomNavHidden = true
                    }
                }

            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()

        this._binding = null
    }
}