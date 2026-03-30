package com.example.netpay.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpay.data.FirebaseRepository
import com.example.netpay.data.models.Balance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _balances = MutableStateFlow<List<FriendBalanceItem>>(emptyList())
    val balances: StateFlow<List<FriendBalanceItem>> = _balances

    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance: StateFlow<Double> = _totalBalance

    init {
        val uid = repository.getCurrentUserId()
        if (uid != null) {
            repository.getBalances { balanceList ->
                var total = 0.0
                val parsed = balanceList.map { bal ->
                    // Determine friend's ID and my net balance
                    val isUser1 = (uid == bal.user1Id)
                    val friendId = if (isUser1) bal.user2Id else bal.user1Id

                    // If I am user1, and netBalance > 0, friend (user2) owes me. So my perspective: +netBalance
                    // If I am user2, and netBalance > 0, I owe user1. So my perspective: -netBalance
                    val myBalance = if (isUser1) bal.netBalance else -bal.netBalance
                    total += myBalance

                    FriendBalanceItem(
                        friendId = friendId,
                        friendName = "User ${friendId.take(5)}", // Would need another query or caching to get real name
                        netBalance = myBalance
                    )
                }
                _totalBalance.value = total
                _balances.value = parsed
            }
        }
    }

    suspend fun settleUp(targetUserId: String) {
        repository.settleUp(targetUserId)
    }
}

data class FriendBalanceItem(
    val friendId: String,
    val friendName: String,
    val netBalance: Double
)
