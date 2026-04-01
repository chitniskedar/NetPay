package com.example.netpay.data.repository

import com.example.netpay.data.model.Balance
import com.example.netpay.data.model.Transaction
import com.example.netpay.data.model.User
import com.google.firebase.auth.FirebaseAuth
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
        
        db.collection("balances")
            .whereArrayContains("users", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val balances = snapshot.toObjects(BalanceData::class.java).map { 
                    Balance(
                        it.pairId, it.user1Id, it.user2Id, it.netBalance, it.localFriendName
                    )
                }
                onUpdate(balances)
            }
    }

    // Real-time listener for transactions with a specific friend
    fun getTransactions(targetUserId: String, onUpdate: (List<Transaction>) -> Unit) {
        val uid = getCurrentUserId() ?: return
        val pairId = Balance.generatePairId(uid, targetUserId)

        db.collection("transactions")
            .whereEqualTo("pairId", pairId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val transactions = snapshot.toObjects(Transaction::class.java)
                onUpdate(transactions)
            }
    }

    // Helper data class for Firestore
    data class BalanceData(
        val pairId: String = "",
        val users: List<String> = emptyList(),
        val user1Id: String = "",
        val user2Id: String = "",
        val netBalance: Double = 0.0,
        val localFriendName: String = ""
    )

    // Helper methods
    suspend fun searchUserById(userId: String): User? {
        val snapshot = db.collection("users").document(userId).get().await()
        return snapshot.toObject(User::class.java)
    }

    suspend fun addTransaction(targetUserId: String, amount: Double, iOweThem: Boolean, note: String = "") {
        val uid = getCurrentUserId() ?: return
        
        // Determine pair info
        val (user1, user2) = Balance.getOrderedIds(uid, targetUserId)
        val pairId = Balance.generatePairId(uid, targetUserId)
        
        // Calculate balance delta from user1's perspective
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
                timestamp = System.currentTimeMillis(),
                description = note
            )
            tx.set(transRef, trans)
        }.await()
    }

    suspend fun settleUp(targetUserId: String) {
        val uid = getCurrentUserId() ?: return
        val pairId = Balance.generatePairId(uid, targetUserId)
        val balanceRef = db.collection("balances").document(pairId)
        
        db.runTransaction { tx ->
            tx.update(balanceRef, "netBalance", 0.0)
        }.await()
    }

    fun addLocalFriend(name: String) {
        val uid = getCurrentUserId() ?: return
        val localFriendId = "local_" + java.util.UUID.randomUUID().toString()
        
        val pairId = Balance.generatePairId(uid, localFriendId)
        val (user1, user2) = Balance.getOrderedIds(uid, localFriendId)
        
        val balanceRef = db.collection("balances").document(pairId)
        
        val newBal = BalanceData(
            pairId = pairId,
            users = listOf(uid), // Only visible to the current user
            user1Id = user1,
            user2Id = user2,
            netBalance = 0.0,
            localFriendName = name
        )
        balanceRef.set(newBal)
    }

    suspend fun removeFriend(targetUserId: String) {
        val uid = getCurrentUserId() ?: return
        val pairId = Balance.generatePairId(uid, targetUserId)
        val balanceRef = db.collection("balances").document(pairId)
        
        val snapshot = balanceRef.get().await()
        val currentBal = snapshot.getDouble("netBalance") ?: 0.0
        
        if (currentBal == 0.0) {
            balanceRef.delete().await()
        } else {
            throw Exception("Balance must be zero to remove friend")
        }
    }

    suspend fun saveUserToDatabase(user: com.google.firebase.auth.FirebaseUser, customName: String = "") {
        val email = user.email ?: ""
        val defaultUsername = email.substringBefore("@").lowercase()
        val userDoc = User(
            userId = user.uid,
            name = if (customName.isNotBlank()) customName else (user.displayName ?: "User"),
            email = email.trim().lowercase(),
            username = defaultUsername
        )
        db.collection("users").document(user.uid).set(userDoc).await()
    }

    /**
     * Deletes the user's Firestore profile, then the Firebase Auth account.
     * If Firebase rejects the delete (session too old), it re-authenticates
     * using [email] + [password] before retrying.
     */
    suspend fun deleteUserProfile(email: String? = null, password: String? = null) {
        val user = auth.currentUser ?: throw Exception("Not signed in")
        val uid = user.uid

        // 1. Delete Firestore document first
        db.collection("users").document(uid).delete().await()

        // 2. Attempt to delete the Firebase Auth record
        try {
            user.delete().await()
        } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
            // Session is stale — reauthenticate if credentials were provided
            if (email != null && password != null) {
                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
                user.reauthenticate(credential).await()
                user.delete().await()
            } else {
                // No credentials provided — sign out so the UI resets, but re-throw
                auth.signOut()
                throw Exception("Session expired. Please log in again and retry deletion.")
            }
        } catch (e: Exception) {
            auth.signOut()
            throw e
        }
    }
}
