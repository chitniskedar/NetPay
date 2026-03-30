package com.example.netpay.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.netpay.R
import com.example.netpay.ui.AuthState
import com.example.netpay.ui.AuthViewModel
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        val emailEdit = findViewById<EditText>(R.id.emailEdit)
        val passEdit = findViewById<EditText>(R.id.passEdit)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val signupBtn = findViewById<Button>(R.id.signupBtn)
        val progress = findViewById<View>(R.id.progressBar)

        loginBtn.setOnClickListener {
            viewModel.authenticate(emailEdit.text.toString(), passEdit.text.toString(), isLogin = true)
        }

        signupBtn.setOnClickListener {
            viewModel.authenticate(emailEdit.text.toString(), passEdit.text.toString(), isLogin = false)
        }

        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> progress.visibility = View.VISIBLE
                    is AuthState.Success -> {
                        progress.visibility = View.GONE
                        startActivity(Intent(this@AuthActivity, HomeActivity::class.java))
                        finish()
                    }
                    is AuthState.Error -> {
                        progress.visibility = View.GONE
                        Toast.makeText(this@AuthActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> progress.visibility = View.GONE
                }
            }
        }
    }
}
