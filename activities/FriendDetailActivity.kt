package com.netpay.activities

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.netpay.R
import com.netpay.data.FirebaseHelper

class FriendDetailActivity : AppCompatActivity() {

    private lateinit var pairId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_detail)

        pairId = intent.getStringExtra("pairId")!!

        val oweBtn = findViewById<Button>(R.id.oweBtn)
        val theyOweBtn = findViewById<Button>(R.id.theyOweBtn)

        oweBtn.setOnClickListener {
            FirebaseHelper.updateBalance(pairId, 100.0, true)
        }

        theyOweBtn.setOnClickListener {
            FirebaseHelper.updateBalance(pairId, 100.0, false)
        }
    }
}