package com.example.netpay.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.netpay.R
import com.example.netpay.ui.AddTransactionViewModel
import com.example.netpay.ui.TxState
import kotlinx.coroutines.launch

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var viewModel: AddTransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        viewModel = ViewModelProvider(this).get(AddTransactionViewModel::class.java)

        val friendEdit = findViewById<EditText>(R.id.friendIdEdit)
        val amountEdit = findViewById<EditText>(R.id.amountEdit)
        val oweRadioGroup = findViewById<RadioGroup>(R.id.oweRadioGroup)
        val iOweThemRadio = findViewById<RadioButton>(R.id.iOweThemRadio)
        val saveBtn = findViewById<Button>(R.id.saveBtn)
        val progress = findViewById<View>(R.id.progressBarTx)

        saveBtn.setOnClickListener {
            val friendId = friendEdit.text.toString()
            val amountStr = amountEdit.text.toString()
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val iOweThem = iOweThemRadio.isChecked

            viewModel.addTransaction(friendId, amount, iOweThem)
        }

        lifecycleScope.launch {
            viewModel.txState.collect { state ->
                when (state) {
                    is TxState.Loading -> progress.visibility = View.VISIBLE
                    is TxState.Success -> {
                        progress.visibility = View.GONE
                        Toast.makeText(this@AddTransactionActivity, "Added successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is TxState.Error -> {
                        progress.visibility = View.GONE
                        Toast.makeText(this@AddTransactionActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> progress.visibility = View.GONE
                }
            }
        }
    }
}
