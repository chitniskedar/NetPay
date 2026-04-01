package com.example.netpay.data.model

data class Balance(
    val pairId: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val netBalance: Double = 0.0,
    val localFriendName: String = ""
) {
    companion object {
        fun generatePairId(uid1: String, uid2: String): String {
            val list = listOf(uid1, uid2).sorted()
            return "${list[0]}_${list[1]}"
        }

        fun getOrderedIds(uid1: String, uid2: String): Pair<String, String> {
            val list = listOf(uid1, uid2).sorted()
            return Pair(list[0], list[1])
        }
    }
}
