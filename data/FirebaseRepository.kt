package com.example.netpay.data

import com.example.netpay.data.models.Balance
import com.example.netpay.data.models.Transaction
import com.example.netpay.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Auth
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Real-time listener for balances
    fun getBalances(onUpdate: (List<Balance>) -> Unit) {
        val uid = getCurrentUserId() ?: return
        
        // A user's balances are those where their ID is either user1Id or user2Id.
        // We have to query both. Wait, Firestore doesn't easily support OR queries like user1Id == uid || user2Id == uid efficiently unless we use an array 'users' field.
        // Let's assume Balance object has a 'users' array of the two IDs for querying.
        // For our model, let me adjust the query. I will query `users` array contains `uid`.
        db.collection("balances")
            .whereArrayContains("users", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val balances = snapshot.toObjects(BalanceData::class.java).map { 
                    Balance(
                        it.pairId, it.user1Id, it.user2Id, it.netBalance
                    )
                }
                onUpdate(balances)
            }
    }

    // Helper data class for Firestore
    data class BalanceData(
        val pairId: String = "",
        val users: List<String> = emptyList(),
        val user1Id: String = "",
        val user2Id: String = "",
        val netBalance: Double = 0.0
    )

    suspend fun addTransaction(targetUserId: String, amount: Double, iOweThem: Boolean) {
        val uid = getCurrentUserId() ?: return
        
        // Determine pair info
        val (user1, user2) = Balance.getOrderedIds(uid, targetUserId)
        val pairId = Balance.generatePairId(uid, targetUserId)
        
        // Calculate balance delta from user1's perspective
        // Reminder: +ve netBalance means user1 owes user2
        val delta = if (uid == user1) {
            if (iOweThem) amount else -amount
        } else {
            if (iOweThem) -amount else amount
        }

        val balanceRef = db.collection("balances").document(pairId)
        val transRef = db.collection("transactions").document()
        
        db.runTransaction { tx ->
            val balanceSnapshot = tx.get(balanceRef)
            if (!balanceSnapshot.exists()) {
                // create new balance entry
                val newBal = BalanceData(
                    pairId = pairId,
                    users = listOf(user1, user2),
                    user1Id = user1,
                    user2Id = user2,
                    netBalance = delta
                )
                tx.set(balanceRef, newBal)
            } else {
                val currentNet = balanceSnapshot.getDouble("netBalance") ?: 0.0
                tx.update(balanceRef, "netBalance", currentNet + delta)
            }

            val trans = Transaction(
                transactionId = transRef.id,
                pairId = pairId,
                fromId = uid,
                toId = targetUserId,
                amount = amount,
                timestamp = System.currentTimeMillis()
            )
            tx.set(transRef, trans)
        }.await()
    }

    suspend fun settleUp(targetUserId: String) {
        val uid = getCurrentUserId() ?: return
        val pairId = Balance.generatePairId(uid, targetUserId)
        val balanceRef = db.collection("balances").document(pairId)
        
        // Reset to 0
        db.runTransaction { tx ->
            tx.update(balanceRef, "netBalance", 0.0)
        }.await()
    }
}
