package com.example.netpay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpay.data.model.FriendBalanceItem
import com.example.netpay.data.model.Transaction
import com.example.netpay.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _balances = MutableStateFlow<List<FriendBalanceItem>>(emptyList())
    val balances: StateFlow<List<FriendBalanceItem>> = _balances

    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance: StateFlow<Double> = _totalBalance

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    init {
        loadBalances()
    }

    private fun loadBalances() {
        val uid = repository.getCurrentUserId()
        if (uid != null) {
            repository.getBalances { balanceList ->
                var total = 0.0
                val parsed = balanceList.map { bal ->
                    val isUser1 = (uid == bal.user1Id)
                    val friendId = if (isUser1) bal.user2Id else bal.user1Id
                    val myBalance = if (isUser1) bal.netBalance else -bal.netBalance
                    total += myBalance

                    // Handle local friend vs legacy network friend
                    val initialName = if (bal.localFriendName.isNotBlank()) bal.localFriendName else "Loading..."
                    val item = FriendBalanceItem(
                        friendId = friendId,
                        friendName = initialName, 
                        netBalance = myBalance
                    )
                    
                    if (bal.localFriendName.isBlank()) {
                        // Fetch real name in background for legacy network friends
                        viewModelScope.launch {
                            val user = repository.searchUserById(friendId)
                            if (user != null) {
                                val currentList = _balances.value.toMutableList()
                                val index = currentList.indexOfFirst { it.friendId == friendId }
                                if (index != -1) {
                                    currentList[index] = currentList[index].copy(friendName = user.name)
                                    _balances.value = currentList
                                }
                            }
                        }
                    }
                    item
                }
                _totalBalance.value = total
                _balances.value = parsed
            }
        }
    }

    fun addFriend(name: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                repository.addLocalFriend(name)
                onResult(true, "Friend added successfully")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to add friend")
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun deleteAccount(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteUserProfile(email, password)
                onResult(true, "Account deleted successfully")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to delete account")
            }
        }
    }

    fun removeFriend(friendId: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.removeFriend(friendId)
                onResult(true, "Friend removed")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to remove friend")
            }
        }
    }

    fun settleUp(targetUserId: String) {
        viewModelScope.launch {
            repository.settleUp(targetUserId)
        }
    }

    fun addTransaction(friendId: String, amount: Double, iOweThem: Boolean, note: String = "") {
        viewModelScope.launch {
            repository.addTransaction(friendId, amount, iOweThem, note)
        }
    }

    fun loadTransactions(friendId: String) {
        repository.getTransactions(friendId) { txList ->
            _transactions.value = txList
        }
    }
}
