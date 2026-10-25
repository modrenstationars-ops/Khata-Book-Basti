package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AccountEditDialog
import com.example.ui.components.CashInDialog
import com.example.ui.components.CashOutDialog
import com.example.ui.components.IncomingDialog
import com.example.ui.components.OutgoingDialog
import com.example.ui.components.SheetRowEntryDialog
import com.example.ui.components.TransferDialog
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.LedgerListScreen
import com.example.ui.screens.SheetViewScreen
import com.example.ui.theme.CashOutRed

enum class AppNavTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    LEDGER("Ledger", Icons.Default.ReceiptLong),
    SHEET("Sheet View", Icons.Default.TableChart),
    ACCOUNTS("Accounts", Icons.Default.AccountBalance)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: KhataViewModel = viewModel()
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(AppNavTab.LEDGER) }

    // States from ViewModel
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val accountsWithBalances by viewModel.accountsWithBalances.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    val selectedAccountId by viewModel.selectedAccountId.collectAsStateWithLifecycle()
    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()

    val activeDialog by viewModel.activeDialog.collectAsStateWithLifecycle()
    val editingAccount by viewModel.editingAccount.collectAsStateWithLifecycle()
    val isAddingAccount by viewModel.isAddingAccount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Khata Ledger",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.shareGoogleSheetCsv(context) },
                        modifier = Modifier.testTag("action_export_csv")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export Google Sheet CSV",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                AppNavTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab == AppNavTab.LEDGER) {
                FloatingActionButton(
                    onClick = { viewModel.openDialog(TransactionDialogType.CashOut) },
                    containerColor = CashOutRed,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.testTag("fab_quick_cash_out")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Quick Cash Out")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppNavTab.LEDGER -> {
                    LedgerListScreen(
                        summary = summary,
                        accountsWithBalances = accountsWithBalances,
                        transactions = filteredTransactions,
                        selectedAccountId = selectedAccountId,
                        dateFilter = dateFilter,
                        searchQuery = searchQuery,
                        typeFilter = typeFilter,
                        onSelectAccount = { viewModel.setSelectedAccount(it) },
                        onDateFilterChange = { viewModel.setDateFilter(it) },
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onTypeFilterChange = { viewModel.setTypeFilter(it) },
                        onOpenDialog = { viewModel.openDialog(it) },
                        onAddAccountClick = { viewModel.openAddAccount() },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) }
                    )
                }
                AppNavTab.SHEET -> {
                    SheetViewScreen(
                        transactions = filteredTransactions,
                        onShareCsv = { viewModel.shareGoogleSheetCsv(it) },
                        onCopyCsv = { viewModel.copyCsvToClipboard(it) },
                        onOpenDialog = { viewModel.openDialog(it) }
                    )
                }
                AppNavTab.ACCOUNTS -> {
                    AccountsScreen(
                        accountsWithBalances = accountsWithBalances,
                        onAddAccountClick = { viewModel.openAddAccount() },
                        onEditAccountClick = { viewModel.openEditAccount(it) },
                        onTransferClick = { viewModel.openDialog(TransactionDialogType.Transfer) }
                    )
                }
            }
        }
    }

    // Dialogs
    when (activeDialog) {
        is TransactionDialogType.CashOut -> {
            CashOutDialog(
                accounts = accounts,
                onDismiss = { viewModel.closeDialog() },
                onConfirm = { accountId, amount, fee, customer, phone, note, ref ->
                    viewModel.addCashOut(accountId, amount, fee, customer, phone, note, ref)
                }
            )
        }
        is TransactionDialogType.CashIn -> {
            CashInDialog(
                accounts = accounts,
                onDismiss = { viewModel.closeDialog() },
                onConfirm = { accountId, amount, fee, customer, phone, note, ref ->
                    viewModel.addCashIn(accountId, amount, fee, customer, phone, note, ref)
                }
            )
        }
        is TransactionDialogType.Incoming -> {
            IncomingDialog(
                accounts = accounts,
                onDismiss = { viewModel.closeDialog() },
                onConfirm = { accountId, amount, fee, customer, note, ref ->
                    viewModel.addIncoming(accountId, amount, fee, customer, note, ref)
                }
            )
        }
        is TransactionDialogType.Outgoing -> {
            OutgoingDialog(
                accounts = accounts,
                onDismiss = { viewModel.closeDialog() },
                onConfirm = { accountId, amount, fee, note, ref ->
                    viewModel.addOutgoing(accountId, amount, fee, note, ref)
                }
            )
        }
        is TransactionDialogType.Transfer -> {
            TransferDialog(
                accounts = accounts,
                onDismiss = { viewModel.closeDialog() },
                onConfirm = { fromId, toId, amount, fee, note ->
                    viewModel.addTransfer(fromId, toId, amount, fee, note)
                }
            )
        }
        is TransactionDialogType.SheetRow -> {
            SheetRowEntryDialog(
                accounts = accounts,
                onDismiss = { viewModel.closeDialog() },
                onConfirm = { accountId, type, cashImpact, accountImpact, amount, fee, customer, note, ref ->
                    viewModel.addSheetRow(accountId, type, cashImpact, accountImpact, amount, fee, customer, note, ref)
                }
            )
        }
        null -> {}
    }

    // Add or Edit Account Dialogs
    if (isAddingAccount) {
        AccountEditDialog(
            account = null,
            onDismiss = { viewModel.closeAddAccount() },
            onSave = { viewModel.saveAccount(it) }
        )
    }

    editingAccount?.let { acc ->
        AccountEditDialog(
            account = acc,
            onDismiss = { viewModel.closeEditAccount() },
            onSave = { viewModel.saveAccount(it) },
            onDelete = { viewModel.deleteAccount(it) }
        )
    }
}
