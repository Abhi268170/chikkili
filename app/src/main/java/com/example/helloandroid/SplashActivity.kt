package com.example.helloandroid

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val splashScreen = installSplashScreen()
            // Keep the splash screen on-screen until the UI is ready.
            splashScreen.setKeepOnScreenCondition { false }
        }

        super.onCreate(savedInstanceState)
        // If Android 12+ is used, the system handles the splash screen
        // and directly launches MainActivity after the splash duration.
        // For older versions, we manually handle the delay and transition.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            setContentView(R.layout.activity_splash) // Set a layout if you want to customize pre-S splash
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 2000) // 2 second delay
        } else {
            // For Android 12+, finish immediately as the system handles the transition
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}