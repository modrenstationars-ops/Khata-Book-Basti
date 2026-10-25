package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val accountType: String, // CASH, JAZZCASH, EASYPAISA, SADAPAY, NAYAPAY, BANK, OTHER
    val accountNumber: String = "",
    val initialBalance: Double = 0.0,
    val colorHex: String = "#059669",
    val isCashDrawer: Boolean = false,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
