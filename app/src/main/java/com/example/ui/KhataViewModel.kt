package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.KhataDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.KhataRepository
import com.example.data.repository.LedgerSummary
import com.example.data.repository.TransactionWithDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateFilter(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL("All Time")
}

sealed class TransactionDialogType {
    object CashOut : TransactionDialogType() // Negative Cash, Positive Account
    object CashIn : TransactionDialogType() // Positive Cash, Negative Account
    object Incoming : TransactionDialogType() // Account Inflow
    object Outgoing : TransactionDialogType() // Account Outflow
    object Transfer : TransactionDialogType() // Between Accounts
    object SheetRow : TransactionDialogType() // Direct Sheet style
}

class KhataViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: KhataRepository

    init {
        val db = KhataDatabase.getDatabase(application)
        repository = KhataRepository(db.khataDao(), application)
        viewModelScope.launch {
            repository.checkAndInitDefaults()
        }
    }

    val accounts: StateFlow<List<AccountEntity>> = repository.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accountsWithBalances: StateFlow<List<AccountWithBalance>> = repository.accountsWithBalances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<LedgerSummary> = repository.summary
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LedgerSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        )

    val allTransactions: StateFlow<List<TransactionWithDetails>> = repository.transactionsWithDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filters
    private val _selectedAccountId = MutableStateFlow<Long?>(null)
    val selectedAccountId: StateFlow<Long?> = _selectedAccountId.asStateFlow()

    private val _dateFilter = MutableStateFlow(DateFilter.ALL)
    val dateFilter: StateFlow<DateFilter> = _dateFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _typeFilter = MutableStateFlow<String?>(null)
    val typeFilter: StateFlow<String?> = _typeFilter.asStateFlow()

    // Active Dialogs
    private val _activeDialog = MutableStateFlow<TransactionDialogType?>(null)
    val activeDialog: StateFlow<TransactionDialogType?> = _activeDialog.asStateFlow()

    private val _editingAccount = MutableStateFlow<AccountEntity?>(null)
    val editingAccount: StateFlow<AccountEntity?> = _editingAccount.asStateFlow()

    private val _isAddingAccount = MutableStateFlow(false)
    val isAddingAccount: StateFlow<Boolean> = _isAddingAccount.asStateFlow()

    private val _selectedTransaction = MutableStateFlow<TransactionWithDetails?>(null)
    val selectedTransaction: StateFlow<TransactionWithDetails?> = _selectedTransaction.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<TransactionWithDetails>> = combine(
        allTransactions,
        _selectedAccountId,
        _dateFilter,
        _searchQuery,
        _typeFilter
    ) { list, accId, dFilter, query, tFilter ->
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val todayStr = dateFormat.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)

        val calWeek = Calendar.getInstance()
        calWeek.set(Calendar.DAY_OF_WEEK, calWeek.firstDayOfWeek)
        val weekStartStr = dateFormat.format(calWeek.time)

        val calMonth = Calendar.getInstance()
        calMonth.set(Calendar.DAY_OF_MONTH, 1)
        val monthStartStr = dateFormat.format(calMonth.time)

        list.filter { item ->
            val t = item.transaction

            // Account filter
            val matchesAccount = (accId == null) || (t.accountId == accId) || (t.toAccountId == accId)

            // Date filter
            val matchesDate = when (dFilter) {
                DateFilter.TODAY -> t.dateString == todayStr
                DateFilter.YESTERDAY -> t.dateString == yesterdayStr
                DateFilter.THIS_WEEK -> t.dateString >= weekStartStr
                DateFilter.THIS_MONTH -> t.dateString >= monthStartStr
                DateFilter.ALL -> true
            }

            // Type filter
            val matchesType = (tFilter == null) || (t.type == tFilter)

            // Search query
            val matchesQuery = query.isBlank() ||
                    item.accountName.contains(query, ignoreCase = true) ||
                    t.customerName.contains(query, ignoreCase = true) ||
                    t.customerPhone.contains(query, ignoreCase = true) ||
                    t.note.contains(query, ignoreCase = true) ||
                    t.referenceId.contains(query, ignoreCase = true)

            matchesAccount && matchesDate && matchesType && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedAccount(accountId: Long?) {
        _selectedAccountId.value = accountId
    }

    fun setDateFilter(filter: DateFilter) {
        _dateFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: String?) {
        _typeFilter.value = type
    }

    fun openDialog(dialog: TransactionDialogType) {
        _activeDialog.value = dialog
    }

    fun closeDialog() {
        _activeDialog.value = null
    }

    fun openEditAccount(account: AccountEntity) {
        _editingAccount.value = account
    }

    fun closeEditAccount() {
        _editingAccount.value = null
    }

    fun openAddAccount() {
        _isAddingAccount.value = true
    }

    fun closeAddAccount() {
        _isAddingAccount.value = false
    }

    fun selectTransaction(transaction: TransactionWithDetails?) {
        _selectedTransaction.value = transaction
    }

    fun setShowExportDialog(show: Boolean) {
        _showExportDialog.value = show
    }

    // 1. CASH OUT: Customer sends digital to Account, takes cash out of drawer
    // User requirement: "cash out ka aik button hu us me value negative dalo"
    fun addCashOut(
        accountId: Long,
        amount: Double,
        fee: Double,
        customerName: String,
        customerPhone: String,
        note: String,
        referenceId: String
    ) {
        viewModelScope.launch {
            val transaction = TransactionEntity(
                type = "CASH_OUT",
                accountId = accountId,
                amount = amount,
                cashImpact = -amount, // Negative value in Cash Drawer as requested!
                accountImpact = +amount, // Account balance increases
                fee = fee,
                feeInCash = true, // Fee earned into shop drawer
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                note = note.trim(),
                referenceId = referenceId.trim()
            )
            repository.addTransaction(transaction)
            closeDialog()
        }
    }

    // 2. CASH IN / ACCOUNT SEND: Customer gives physical cash to shop, shop sends digital money from Account
    // User requirement: "r agr acount me se kisi aik account se paise send hun tu cash out/in me positive value dalon"
    fun addCashIn(
        accountId: Long,
        amount: Double,
        fee: Double,
        customerName: String,
        customerPhone: String,
        note: String,
        referenceId: String
    ) {
        viewModelScope.launch {
            val transaction = TransactionEntity(
                type = "CASH_IN",
                accountId = accountId,
                amount = amount,
                cashImpact = +amount, // Positive value in Cash Drawer as requested!
                accountImpact = -amount, // Account balance deducted
                fee = fee,
                feeInCash = true,
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                note = note.trim(),
                referenceId = referenceId.trim()
            )
            repository.addTransaction(transaction)
            closeDialog()
        }
    }

    // 3. INCOMING: Direct deposit/transfer into an account
    fun addIncoming(
        accountId: Long,
        amount: Double,
        fee: Double,
        customerName: String,
        note: String,
        referenceId: String
    ) {
        viewModelScope.launch {
            val transaction = TransactionEntity(
                type = "INCOMING",
                accountId = accountId,
                amount = amount,
                cashImpact = 0.0,
                accountImpact = +amount,
                fee = fee,
                feeInCash = false,
                customerName = customerName.trim(),
                note = note.trim(),
                referenceId = referenceId.trim()
            )
            repository.addTransaction(transaction)
            closeDialog()
        }
    }

    // 4. OUTGOING: Direct withdrawal/expense from an account
    fun addOutgoing(
        accountId: Long,
        amount: Double,
        fee: Double,
        note: String,
        referenceId: String
    ) {
        viewModelScope.launch {
            val transaction = TransactionEntity(
                type = "OUTGOING",
                accountId = accountId,
                amount = amount,
                cashImpact = 0.0,
                accountImpact = -amount,
                fee = fee,
                feeInCash = false,
                note = note.trim(),
                referenceId = referenceId.trim()
            )
            repository.addTransaction(transaction)
            closeDialog()
        }
    }

    // 5. TRANSFER: Transfer between two digital accounts
    fun addTransfer(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        fee: Double,
        note: String
    ) {
        viewModelScope.launch {
            val transaction = TransactionEntity(
                type = "TRANSFER",
                accountId = fromAccountId,
                toAccountId = toAccountId,
                amount = amount,
                cashImpact = 0.0,
                accountImpact = -amount,
                fee = fee,
                feeInCash = false,
                note = note.trim()
            )
            repository.addTransaction(transaction)
            closeDialog()
        }
    }

    // 6. DIRECT SHEET ROW ENTRY (Mimics typing directly into a Google Sheet row)
    fun addSheetRow(
        accountId: Long,
        type: String,
        cashImpact: Double,
        accountImpact: Double,
        amount: Double,
        fee: Double,
        customerName: String,
        note: String,
        referenceId: String
    ) {
        viewModelScope.launch {
            val transaction = TransactionEntity(
                type = type,
                accountId = accountId,
                amount = amount,
                cashImpact = cashImpact,
                accountImpact = accountImpact,
                fee = fee,
                feeInCash = true,
                customerName = customerName.trim(),
                note = note.trim(),
                referenceId = referenceId.trim()
            )
            repository.addTransaction(transaction)
            closeDialog()
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
            _selectedTransaction.value = null
        }
    }

    fun saveAccount(account: AccountEntity) {
        viewModelScope.launch {
            if (account.id == 0L) {
                repository.addAccount(account)
            } else {
                repository.updateAccount(account)
            }
            closeAddAccount()
            closeEditAccount()
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            closeEditAccount()
        }
    }

    fun shareGoogleSheetCsv(context: Context) {
        viewModelScope.launch {
            try {
                val list = filteredTransactions.value
                val file = repository.generateGoogleSheetCsv(list)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Khata Ledger Export - Google Sheets")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Khata Ledger transactions export (${list.size} records). Compatible with Google Sheets and Excel."
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(intent, "Share / Open Google Sheet CSV")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun copyCsvToClipboard(context: Context) {
        viewModelScope.launch {
            try {
                val list = filteredTransactions.value
                val csvContent = repository.getCsvContentString(list)
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Khata Ledger CSV", csvContent)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied CSV to clipboard! Paste directly into Google Sheets.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error copying: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
