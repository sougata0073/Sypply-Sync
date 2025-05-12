package com.sougata.supplysync

import android.os.Bundle
import android.view.Menu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.sougata.supplysync.databinding.ActivityMainBinding
import com.sougata.supplysync.util.AnimationProvider

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    private var isBottomNavHidden = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        this._binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        this.binding.lifecycleOwner = this

        this.setSupportActionBar(this.binding.toolBar)

        this.supportActionBar?.title = "Home"

        this.setupBottomNav()

        this.binding.toolBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> {
                    this.navController.navigate(R.id.settingsFragment)
                }
            }
            true
        }
    }

    private fun setupBottomNav() {

        this.navController =
            (this.supportFragmentManager.findFragmentById(R.id.navHostMain) as NavHostFragment).navController

        val popupMenu = PopupMenu(this, this.binding.bottomNav)
        popupMenu.inflate(R.menu.bottom_nav_menu)
        val menu = popupMenu.menu
        this.binding.bottomNav.setupWithNavController(menu, this.navController)

        val bottomNavAnimator = AnimationProvider(this.binding.bottomNav)
        val bottomNavBorderAnimator = AnimationProvider(this.binding.bottomNavBorder)

        this.navController.addOnDestinationChangedListener { navController, destination, arguments ->

            when (destination.id) {
                R.id.homeFragment -> this.supportActionBar?.title = "Home"
                R.id.suppliersHomeFragment -> this.supportActionBar?.title = "Suppliers Dashboard"
                R.id.customersHomeFragment -> this.supportActionBar?.title = "Customers Dashboard"
                R.id.staffsHomeFragment -> this.supportActionBar?.title = "Staffs Dashboard"
            }

            when (destination.id) {

                R.id.homeFragment, R.id.staffsHomeFragment,
                R.id.customersHomeFragment, R.id.suppliersHomeFragment -> {
                    if (this.isBottomNavHidden) {
                        bottomNavAnimator.slideUp()
                        bottomNavBorderAnimator.slideUp(this.binding.bottomNav.height.toFloat())
                        this.isBottomNavHidden = false
                    }
                    if (this.supportActionBar?.isShowing == false) {
                        this.supportActionBar?.show()
                    }
                }

                else -> {
                    if (!this.isBottomNavHidden) {
                        bottomNavAnimator.slideDown()
                        bottomNavBorderAnimator.slideDown(this.binding.bottomNav.height.toFloat())
                        this.isBottomNavHidden = true
                    }
                    if (this.supportActionBar?.isShowing == true) {
                        this.supportActionBar?.hide()
                    }
                }

            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        this._binding = null
    }
}