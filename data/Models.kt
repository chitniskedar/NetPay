package com.netpay.data

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = ""
)

data class Balance(
    val pairId: String = "",
    val users: List<String> = listOf(),
    val balance: Double = 0.0
)