package com.netpay.activities

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.netpay.R
import com.netpay.data.FirebaseHelper

class AddFriendActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        val addBtn = findViewById<Button>(R.id.addBtn)

        addBtn.setOnClickListener {
            val currentUser = FirebaseHelper.getCurrentUserId()!!
            val friendId = "ENTER_FRIEND_ID"

            FirebaseHelper.createBalance(currentUser, friendId)
        }
    }
}