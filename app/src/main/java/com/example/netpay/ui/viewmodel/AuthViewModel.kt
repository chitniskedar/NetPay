package com.example.netpay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpay.data.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val repository = FirebaseRepository()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // simple email login / signup
    fun authenticate(email: String, pass: String, isLogin: Boolean, name: String = "") {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = if (isLogin) {
                    auth.signInWithEmailAndPassword(email, pass).await()
                } else {
                    auth.createUserWithEmailAndPassword(email, pass).await()
                }
                result.user?.let { repository.saveUserToDatabase(it, name) }
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
