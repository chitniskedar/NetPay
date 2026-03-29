package com.netpay.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseHelper {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    //  Register
    fun register(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val userId = auth.currentUser!!.uid
                val user = User(userId, name, email)

                db.collection("users").document(userId).set(user)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it.message ?: "Error") }
            }
            .addOnFailureListener {
                onFailure(it.message ?: "Error")
            }
    }

    //  Login
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Error") }
    }

    //  Add Friend (creates balance doc)
    fun createBalance(user1: String, user2: String) {
        val pairId = listOf(user1, user2).sorted().joinToString("_")

        val balance = Balance(
            pairId = pairId,
            users = listOf(user1, user2),
            balance = 0.0
        )

        db.collection("balances").document(pairId).set(balance)
    }

    //  Update Balance
    fun updateBalance(pairId: String, amount: Double, isYouOwe: Boolean) {
        val delta = if (isYouOwe) -amount else +amount

        db.collection("balances")
            .document(pairId)
            .update("balance", FieldValue.increment(delta))
    }

    //  Fetch Balances (real-time)
    fun getBalances(
        userId: String,
        onUpdate: (List<Balance>) -> Unit
    ) {
        db.collection("balances")
            .whereArrayContains("users", userId)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(Balance::class.java) ?: listOf()
                onUpdate(list)
            }
    }
}