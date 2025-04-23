package com.mtl.My_Hack_X

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MyHackXApp : Application() {
    companion object {
        private const val TAG = "MyHackXApp"
        
        // Static variable accessible throughout the app
        var isTestMode: Boolean = true
    }
    
    override fun onCreate() {
        super.onCreate()
        try {
            // Try to initialize Firebase first
            if (!isFirebaseInitialized()) {
                try {
                    FirebaseApp.initializeApp(this)
                    Log.d(TAG, "Firebase initialized in Application class")
                    isTestMode = false
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase initialization failed in Application class", e)
                    // Keep test mode true
                    setUpTestFirebase()
                }
            } else {
                Log.d(TAG, "Firebase was already initialized")
                isTestMode = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in application setup", e)
            setUpTestFirebase()
        }
    }
    
    private fun setUpTestFirebase() {
        try {
            // Only create test Firebase if none exists
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:123456789000:android:1234567890abcdef")
                    .setApiKey("AIzaSyDummyValueForTesting123456789")
                    .setProjectId("my-hackx-test")
                    .build()
                
                FirebaseApp.initializeApp(this, options, "test")
                Log.d(TAG, "Test Firebase instance created in Application")
                isTestMode = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create test Firebase instance", e)
            isTestMode = true
        }
    }
    
    private fun isFirebaseInitialized(): Boolean {
        return try {
            FirebaseApp.getInstance() != null
        } catch (e: Exception) {
            false
        }
    }
} 