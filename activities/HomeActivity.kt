package com.example.netpay.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.netpay.R
import com.example.netpay.ui.FriendAdapter
import com.example.netpay.ui.HomeViewModel
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: FriendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val totalBalanceTxt = findViewById<TextView>(R.id.totalBalance)
        val recycler = findViewById<RecyclerView>(R.id.friendRecycler)

        val addTxBtn = findViewById<Button>(R.id.addTxBtn)
        addTxBtn.setOnClickListener {
            startActivity(Intent(this@HomeActivity, AddTransactionActivity::class.java))
        }

        adapter = FriendAdapter(
            onSettleUp = { friendId ->
                lifecycleScope.launch {
                    viewModel.settleUp(friendId)
                }
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        lifecycleScope.launch {
            viewModel.totalBalance.collect { total ->
                totalBalanceTxt.text = if (total >= 0) "₹$total" else "-₹${-total}"
                totalBalanceTxt.setTextColor(if (total >= 0) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
            }
        }

        lifecycleScope.launch {
            viewModel.balances.collect { items ->
                adapter.submitList(items)
            }
        }
    }
}
