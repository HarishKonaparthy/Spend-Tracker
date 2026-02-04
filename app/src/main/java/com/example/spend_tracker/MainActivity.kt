package com.example.spend_tracker

import android.os.Bundle
import android.view.Menu
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.spend_tracker.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import kotlin.math.abs
import android.widget.TextView
import com.example.spend_tracker.timer.RunningTimer

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    // Added gesture detector
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Set device name in navigation header (WITHOUT NavHeaderManager)
        val headerView = navView.getHeaderView(0)
        val tvDeviceName = headerView.findViewById<TextView>(R.id.tvDeviceName)
        val tvTimestamp = headerView.findViewById<TextView>(R.id.tvtimestamp)
        val deviceName = com.example.spend_tracker.device.DeviceInfo.getDeviceName()
        tvDeviceName.text = deviceName
        // Start running timer
        val timer = RunningTimer { time ->
            val finalTimestamp = "$time"  // YYYY-MMM-DD-HH:MM:ss
            tvTimestamp.text = finalTimestamp   // updates every second
        }

        timer.start()

        timer.start()

        // Configure navigation destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_credit,
                R.id.nav_debit,
                R.id.nav_lendsend,
                R.id.nav_lendreceive,
                R.id.nav_lendreminder,
                R.id.nav_view,
                R.id.nav_dropdown,
                R.id.nav_pw,
                R.id.nav_vehicle
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Initialize gesture detector
        gestureDetector = GestureDetector(this, SwipeGestureListener())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Enable gesture detection
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {

        // Fixed naming (camelCase instead of underscores)
        private val swipeThreshold = 100
        private val swipeVelocityThreshold = 100

        override fun onFling(
            e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
        ): Boolean {
            if (e1 != null) {
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                // Horizontal swipe only
                if (abs(diffX) > abs(diffY)) {

                    if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {

                        if (diffX > 0) {
                            // → Swipe Right → Open drawer
                            if (!drawerLayout.isDrawerOpen(binding.navView)) {
                                drawerLayout.openDrawer(binding.navView)
                            }
                        } else {
                            // ← Swipe Left → Close drawer
                            if (drawerLayout.isDrawerOpen(binding.navView)) {
                                drawerLayout.closeDrawer(binding.navView)
                            }
                        }
                        return true
                    }
                }
            }
            return false
        }
    }
}