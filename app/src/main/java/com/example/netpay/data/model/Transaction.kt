package com.example.netpay.data.model

data class Transaction(
    val transactionId: String = "",
    val pairId: String = "",
    val fromId: String = "", // who paid
    val toId: String = "",   // who received
    val amount: Double = 0.0,
    val timestamp: Long = 0L,
    val description: String = ""
)
