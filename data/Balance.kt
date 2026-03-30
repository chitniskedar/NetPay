package com.example.netpay.data.models

/**
 * Balance represents the net balance between two users.
 * To ensure uniqueness and correct perspective:
 * - [user1Id] is always the lexicographically smaller ID.
 * - [user2Id] is always the larger ID.
 * 
 * If [netBalance] is positive, user1 owes user2.
 * If [netBalance] is negative, user2 owes user1.
 */
data class Balance(
    val pairId: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val netBalance: Double = 0.0 // from user1's perspective: +ve means user1 owes user2
) {
    companion object {
        fun generatePairId(idA: String, idB: String): String {
            return if (idA < idB) "${idA}_${idB}" else "${idB}_${idA}"
        }

        fun getOrderedIds(idA: String, idB: String): Pair<String, String> {
            return if (idA < idB) Pair(idA, idB) else Pair(idB, idA)
        }
    }
}
