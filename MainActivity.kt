package com.example.netpay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.netpay.activities.AuthActivity
import com.example.netpay.activities.HomeActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Routing Logic
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            // User is signed in
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // User is not signed in
            startActivity(Intent(this, AuthActivity::class.java))
        }
        finish() // Prevent going back to this empty routing activity
    }
}
