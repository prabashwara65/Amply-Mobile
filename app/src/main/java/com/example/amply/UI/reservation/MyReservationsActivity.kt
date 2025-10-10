package com.example.amply.ui.reservation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.amply.R
import com.example.amply.ui.dashboard.EvOwnerDashboard
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MyReservationsActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var bottomNavigation: BottomNavigationView

    // Called when activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_reservations)

        // Initialize views
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Setup ViewPager with adapter
        setupViewPager()

        // Setup bottom navigation bar
        setupBottomNavigation()
    }

    // Sets up the ViewPager2 with its adapter and connects it to TabLayout
    private fun setupViewPager() {
        val adapter = ReservationPagerAdapter(this)
        viewPager.adapter = adapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Pending"
                1 -> "Confirmed"
                2 -> "Done"
                else -> ""
            }
        }.attach()
    }

    // ViewPager2 Adapter
    private inner class ReservationPagerAdapter(activity: FragmentActivity) :
        FragmentStateAdapter(activity) {

        // Total number of tabs
        override fun getItemCount(): Int = 3

        // Returns appropriate fragment based on tab position
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ReservationListFragment.newInstance("Pending")
                1 -> ReservationListFragment.newInstance("Confirmed")
                2 -> ReservationListFragment.newInstance("Done")
                else -> ReservationListFragment.newInstance("Pending")
            }
        }
    }

    // Sets up bottom navigation and handles item clicks
    private fun setupBottomNavigation() {
        // Set Activity tab as selected
        bottomNavigation.selectedItemId = R.id.nav_activity
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    val intent = Intent(this, EvOwnerDashboard::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_activity -> {
                    // Already on this page
                    true
                }
                R.id.nav_saved -> {
                    // Navigate to Saved/Bookmarks page
                    true
                }
                R.id.nav_account -> {
                    // Navigate to Account/Profile page
                    true
                }
                else -> false
            }
        }
    }
}
