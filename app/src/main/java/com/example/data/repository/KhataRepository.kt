package com.example.data.repository

import android.content.Context
import androidx.core.content.FileProvider
import com.example.data.dao.KhataDao
import com.example.data.database.KhataDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AccountWithBalance(
    val account: AccountEntity,
    val currentBalance: Double,
    val totalIn: Double,
    val totalOut: Double,
    val totalFees: Double
)

data class TransactionWithDetails(
    val transaction: TransactionEntity,
    val accountName: String,
    val accountType: String,
    val toAccountName: String? = null,
    val accountBalanceAfter: Double = 0.0,
    val cashBalanceAfter: Double = 0.0
)

data class LedgerSummary(
    val totalNetBalance: Double,
    val cashDrawerBalance: Double,
    val totalDigitalBalance: Double,
    val todayCashIn: Double,
    val todayCashOut: Double,
    val todayFees: Double,
    val totalFees: Double
)

class KhataRepository(
    private val khataDao: KhataDao,
    private val context: Context
) {
    val accounts: Flow<List<AccountEntity>> = khataDao.getAllAccounts()
    val transactions: Flow<List<TransactionEntity>> = khataDao.getAllTransactions()

    suspend fun checkAndInitDefaults() {
        withContext(Dispatchers.IO) {
            val count = khataDao.getAccountsCount()
            if (count == 0) {
                khataDao.insertAccounts(KhataDatabase.getInitialAccounts())
            }
        }
    }

    suspend fun addAccount(account: AccountEntity): Long = withContext(Dispatchers.IO) {
        khataDao.insertAccount(account)
    }

    suspend fun updateAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        khataDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        khataDao.deleteAccount(account)
    }

    suspend fun addTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateStr = if (transaction.dateString.isBlank()) dateFormat.format(Date(now)) else transaction.dateString
        val timeStr = if (transaction.timeString.isBlank()) timeFormat.format(Date(now)) else transaction.timeString
        val ts = if (transaction.timestamp == 0L) now else transaction.timestamp

        khataDao.insertTransaction(
            transaction.copy(
                timestamp = ts,
                dateString = dateStr,
                timeString = timeStr
            )
        )
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        khataDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        khataDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) = withContext(Dispatchers.IO) {
        khataDao.deleteTransactionById(id)
    }

    suspend fun clearAllTransactions() = withContext(Dispatchers.IO) {
        khataDao.clearAllTransactions()
    }

    suspend fun resetToDefaultAccounts() = withContext(Dispatchers.IO) {
        khataDao.clearAllTransactions()
        for (acc in KhataDatabase.getInitialAccounts()) {
            khataDao.insertAccount(acc)
        }
    }

    // Combine accounts and transactions into live AccountWithBalance list
    val accountsWithBalances: Flow<List<AccountWithBalance>> = combine(accounts, transactions) { accList, trxList ->
        val cashDrawer = accList.firstOrNull { it.isCashDrawer }

        accList.map { acc ->
            var currentBalance = acc.initialBalance
            var totalIn = 0.0
            var totalOut = 0.0
            var totalFees = 0.0

            if (acc.isCashDrawer) {
                // Physical Cash Drawer: impacted by cashImpact and cash fees
                for (trx in trxList) {
                    currentBalance += trx.cashImpact
                    if (trx.cashImpact > 0) totalIn += trx.cashImpact
                    if (trx.cashImpact < 0) totalOut += -trx.cashImpact

                    if (trx.feeInCash && trx.fee > 0) {
                        currentBalance += trx.fee
                        totalFees += trx.fee
                    }
                }
            } else {
                // Digital Account (JazzCash, EasyPaisa, Bank, etc.)
                for (trx in trxList) {
                    if (trx.accountId == acc.id) {
                        currentBalance += trx.accountImpact
                        if (trx.accountImpact > 0) totalIn += trx.accountImpact
                        if (trx.accountImpact < 0) totalOut += -trx.accountImpact
                        totalFees += trx.fee
                    } else if (trx.type == "TRANSFER" && trx.toAccountId == acc.id) {
                        currentBalance += trx.amount
                        totalIn += trx.amount
                    }
                }
            }

            AccountWithBalance(
                account = acc,
                currentBalance = currentBalance,
                totalIn = totalIn,
                totalOut = totalOut,
                totalFees = totalFees
            )
        }
    }

    // Combine into ledger summary
    val summary: Flow<LedgerSummary> = combine(accountsWithBalances, transactions) { accBalances, trxList ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        var cashDrawerBalance = 0.0
        var totalDigitalBalance = 0.0

        for (item in accBalances) {
            if (item.account.isCashDrawer) {
                cashDrawerBalance += item.currentBalance
            } else {
                totalDigitalBalance += item.currentBalance
            }
        }

        var todayCashIn = 0.0
        var todayCashOut = 0.0
        var todayFees = 0.0
        var totalFees = 0.0

        for (trx in trxList) {
            totalFees += trx.fee

            if (trx.dateString == todayStr) {
                todayFees += trx.fee
                if (trx.cashImpact > 0) {
                    todayCashIn += trx.cashImpact
                } else if (trx.cashImpact < 0) {
                    todayCashOut += -trx.cashImpact
                }
            }
        }

        LedgerSummary(
            totalNetBalance = cashDrawerBalance + totalDigitalBalance,
            cashDrawerBalance = cashDrawerBalance,
            totalDigitalBalance = totalDigitalBalance,
            todayCashIn = todayCashIn,
            todayCashOut = todayCashOut,
            todayFees = todayFees,
            totalFees = totalFees
        )
    }

    // Enriched transactions with running balance for the Google Sheet Ledger view
    val transactionsWithDetails: Flow<List<TransactionWithDetails>> = combine(accounts, transactions) { accList, trxList ->
        val accMap = accList.associateBy { it.id }
        val cashAccount = accList.firstOrNull { it.isCashDrawer }

        // Sort ascending to calculate running balance, then reverse for display
        val sortedAsc = trxList.sortedWith(compareBy({ it.timestamp }, { it.id }))

        val runningAccountBalance = mutableMapOf<Long, Double>()
        for (acc in accList) {
            runningAccountBalance[acc.id] = acc.initialBalance
        }
        var runningCashBalance = cashAccount?.initialBalance ?: 0.0

        val enrichedList = sortedAsc.map { trx ->
            // Update running cash balance
            runningCashBalance += trx.cashImpact
            if (trx.feeInCash && trx.fee > 0) {
                runningCashBalance += trx.fee
            }

            // Update running account balance
            val currentAccBal = runningAccountBalance[trx.accountId] ?: 0.0
            val newAccBal = currentAccBal + trx.accountImpact
            runningAccountBalance[trx.accountId] = newAccBal

            if (trx.type == "TRANSFER" && trx.toAccountId != null) {
                val toAccBal = runningAccountBalance[trx.toAccountId] ?: 0.0
                runningAccountBalance[trx.toAccountId] = toAccBal + trx.amount
            }

            TransactionWithDetails(
                transaction = trx,
                accountName = accMap[trx.accountId]?.name ?: "Account #${trx.accountId}",
                accountType = accMap[trx.accountId]?.accountType ?: "OTHER",
                toAccountName = trx.toAccountId?.let { accMap[it]?.name },
                accountBalanceAfter = newAccBal,
                cashBalanceAfter = runningCashBalance
            )
        }

        enrichedList.asReversed()
    }

    // Generate CSV file formatted for Google Sheets
    suspend fun generateGoogleSheetCsv(transactions: List<TransactionWithDetails>): File = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val csvFile = File(exportDir, "khata_ledger_$timestamp.csv")

        FileWriter(csvFile).use { writer ->
            // Header matching Google Sheets
            writer.append("Date,Time,Transaction_Type,Account,Cash_In_Out_Value,Account_Impact,Fee_Commission,Account_Balance_After,Cash_Balance_After,Customer_Name,Phone,Reference_ID,Notes\n")

            for (item in transactions) {
                val t = item.transaction
                val cashFormatted = when {
                    t.cashImpact > 0 -> "+${String.format(Locale.US, "%.2f", t.cashImpact)}"
                    t.cashImpact < 0 -> String.format(Locale.US, "%.2f", t.cashImpact)
                    else -> "0.00"
                }
                val accFormatted = when {
                    t.accountImpact > 0 -> "+${String.format(Locale.US, "%.2f", t.accountImpact)}"
                    t.accountImpact < 0 -> String.format(Locale.US, "%.2f", t.accountImpact)
                    else -> "0.00"
                }
                val typeLabel = when (t.type) {
                    "CASH_OUT" -> "Cash Out (- Cash / + Account)"
                    "CASH_IN" -> "Cash In (+ Cash / - Account)"
                    "INCOMING" -> "Incoming (+ Account)"
                    "OUTGOING" -> "Outgoing (- Account)"
                    "TRANSFER" -> "Transfer (${item.accountName} -> ${item.toAccountName ?: "Account"})"
                    else -> t.type
                }

                fun escapeCsv(s: String): String {
                    return if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
                        "\"${s.replace("\"", "\"\"")}\""
                    } else s
                }

                writer.append("${t.dateString},")
                writer.append("${t.timeString},")
                writer.append("${escapeCsv(typeLabel)},")
                writer.append("${escapeCsv(item.accountName)},")
                writer.append("$cashFormatted,")
                writer.append("$accFormatted,")
                writer.append("${String.format(Locale.US, "%.2f", t.fee)},")
                writer.append("${String.format(Locale.US, "%.2f", item.accountBalanceAfter)},")
                writer.append("${String.format(Locale.US, "%.2f", item.cashBalanceAfter)},")
                writer.append("${escapeCsv(t.customerName)},")
                writer.append("${escapeCsv(t.customerPhone)},")
                writer.append("${escapeCsv(t.referenceId)},")
                writer.append("${escapeCsv(t.note)}\n")
            }
        }

        csvFile
    }

    fun getCsvContentString(transactions: List<TransactionWithDetails>): String {
        val sb = StringBuilder()
        sb.append("Date,Time,Transaction Type,Account,Cash In/Out,Account Impact,Fee,Acc Balance,Cash Balance,Customer,Reference ID,Notes\n")
        for (item in transactions) {
            val t = item.transaction
            val cashFormatted = when {
                t.cashImpact > 0 -> "+${t.cashImpact}"
                t.cashImpact < 0 -> "${t.cashImpact}"
                else -> "0"
            }
            sb.append("${t.dateString},${t.timeString},${t.type},\"${item.accountName}\",$cashFormatted,${t.accountImpact},${t.fee},${item.accountBalanceAfter},${item.cashBalanceAfter},\"${t.customerName}\",\"${t.referenceId}\",\"${t.note}\"\n")
        }
        return sb.toString()
    }
}
