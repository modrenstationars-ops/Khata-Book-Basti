package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.KhataDao
import com.example.data.model.AccountEntity
import com.example.data.model.TransactionEntity

@Database(
    entities = [AccountEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KhataDatabase : RoomDatabase() {
    abstract fun khataDao(): KhataDao

    companion object {
        @Volatile
        private var INSTANCE: KhataDatabase? = null

        fun getDatabase(context: Context): KhataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KhataDatabase::class.java,
                    "khata_ledger_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getInitialAccounts(): List<AccountEntity> {
            return listOf(
                AccountEntity(
                    id = 1,
                    name = "Cash Drawer (دکان کیش)",
                    accountType = "CASH",
                    accountNumber = "Shop Drawer",
                    initialBalance = 25000.0,
                    colorHex = "#16A34A",
                    isCashDrawer = true,
                    orderIndex = 0
                ),
                AccountEntity(
                    id = 2,
                    name = "JazzCash 1",
                    accountType = "JAZZCASH",
                    accountNumber = "0300-1234567",
                    initialBalance = 20000.0,
                    colorHex = "#DC2626",
                    isCashDrawer = false,
                    orderIndex = 1
                ),
                AccountEntity(
                    id = 3,
                    name = "JazzCash 2",
                    accountType = "JAZZCASH",
                    accountNumber = "0302-7654321",
                    initialBalance = 15000.0,
                    colorHex = "#EA580C",
                    isCashDrawer = false,
                    orderIndex = 2
                ),
                AccountEntity(
                    id = 4,
                    name = "EasyPaisa 1",
                    accountType = "EASYPAISA",
                    accountNumber = "0345-1234567",
                    initialBalance = 20000.0,
                    colorHex = "#059669",
                    isCashDrawer = false,
                    orderIndex = 3
                ),
                AccountEntity(
                    id = 5,
                    name = "EasyPaisa 2",
                    accountType = "EASYPAISA",
                    accountNumber = "0346-9876543",
                    initialBalance = 12000.0,
                    colorHex = "#0D9488",
                    isCashDrawer = false,
                    orderIndex = 4
                ),
                AccountEntity(
                    id = 6,
                    name = "NayaPay / SadaPay",
                    accountType = "SADAPAY",
                    accountNumber = "0312-3456789",
                    initialBalance = 15000.0,
                    colorHex = "#0284C7",
                    isCashDrawer = false,
                    orderIndex = 5
                ),
                AccountEntity(
                    id = 7,
                    name = "Bank Account",
                    accountType = "BANK",
                    accountNumber = "PK12MEZN0001...",
                    initialBalance = 35000.0,
                    colorHex = "#4F46E5",
                    isCashDrawer = false,
                    orderIndex = 6
                )
            )
        }
    }
}
