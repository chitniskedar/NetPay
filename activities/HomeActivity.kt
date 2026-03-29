package com.netpay.activities

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.netpay.R
import com.netpay.data.FirebaseHelper

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val userId = FirebaseHelper.getCurrentUserId() ?: return

        FirebaseHelper.getBalances(userId) { balances ->
            // TODO: connect to adapter
            println(balances)
        }
    }
}