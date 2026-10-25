package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["toAccountId"]),
        Index(value = ["dateString"]),
        Index(value = ["timestamp"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = "", // e.g. "2026-10-25"
    val timeString: String = "", // e.g. "14:30"
    val type: String, // CASH_OUT, CASH_IN, INCOMING, OUTGOING, TRANSFER
    val accountId: Long, // Account sending or receiving
    val toAccountId: Long? = null, // In case of account-to-account transfer
    val amount: Double, // Absolute transaction amount
    val cashImpact: Double = 0.0, // Impact on physical cash drawer (-amount for cash out, +amount for cash in)
    val accountImpact: Double = 0.0, // Impact on digital account balance (+amount for incoming/cash out, -amount for outgoing/cash in)
    val fee: Double = 0.0, // Fee / Commission charged
    val feeInCash: Boolean = true, // Whether fee was added to physical cash drawer
    val customerName: String = "",
    val customerPhone: String = "",
    val note: String = "",
    val referenceId: String = "" // Trx ID or sheet row
)
