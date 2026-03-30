package com.example.netpay.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpay.data.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddTransactionViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _txState = MutableStateFlow<TxState>(TxState.Idle)
    val txState: StateFlow<TxState> = _txState

    fun addTransaction(friendId: String, amount: Double, iOweThem: Boolean) {
        if (friendId.isBlank() || amount <= 0) {
            _txState.value = TxState.Error("Valid Friend ID and Amount required")
            return
        }
        
        viewModelScope.launch {
            _txState.value = TxState.Loading
            try {
                repository.addTransaction(friendId, amount, iOweThem)
                _txState.value = TxState.Success
            } catch (e: Exception) {
                _txState.value = TxState.Error(e.message ?: "Failed to add transaction")
            }
        }
    }
}

sealed class TxState {
    object Idle : TxState()
    object Loading : TxState()
    object Success : TxState()
    data class Error(val message: String) : TxState()
}
