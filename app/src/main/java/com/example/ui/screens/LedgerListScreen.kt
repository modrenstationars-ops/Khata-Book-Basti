package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.LedgerSummary
import com.example.data.repository.TransactionWithDetails
import com.example.ui.DateFilter
import com.example.ui.TransactionDialogType
import com.example.ui.components.AccountChipsRow
import com.example.ui.components.QuickActionButtons
import com.example.ui.components.StatSummaryCards
import com.example.ui.theme.CashInGreen
import com.example.ui.theme.CashInGreenLight
import com.example.ui.theme.CashOutRed
import com.example.ui.theme.CashOutRedLight
import com.example.ui.theme.FeeGold
import com.example.ui.theme.FeeGoldLight
import com.example.ui.theme.SlateSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LedgerListScreen(
    summary: LedgerSummary,
    accountsWithBalances: List<AccountWithBalance>,
    transactions: List<TransactionWithDetails>,
    selectedAccountId: Long?,
    dateFilter: DateFilter,
    searchQuery: String,
    typeFilter: String?,
    onSelectAccount: (Long?) -> Unit,
    onDateFilterChange: (DateFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onTypeFilterChange: (String?) -> Unit,
    onOpenDialog: (TransactionDialogType) -> Unit,
    onAddAccountClick: () -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var transactionToDelete by remember { mutableStateOf<TransactionWithDetails?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Top Summary Cards (Cash Drawer, Net Worth, Today's Totals & Fees)
        item {
            StatSummaryCards(summary = summary)
        }

        // 2. 6-7 Accounts Live Balance Chips
        item {
            AccountChipsRow(
                accountsWithBalances = accountsWithBalances,
                selectedAccountId = selectedAccountId,
                onSelectAccount = onSelectAccount,
                onAddAccountClick = onAddAccountClick
            )
        }

        // 3. Quick Action Buttons (Cash Out [-], Cash In [+], + Incoming, Transfer, Sheet Row)
        item {
            QuickActionButtons(
                onCashOutClick = { onOpenDialog(TransactionDialogType.CashOut) },
                onCashInClick = { onOpenDialog(TransactionDialogType.CashIn) },
                onIncomingClick = { onOpenDialog(TransactionDialogType.Incoming) },
                onTransferClick = { onOpenDialog(TransactionDialogType.Transfer) },
                onSheetRowClick = { onOpenDialog(TransactionDialogType.SheetRow) }
            )
        }

        // 4. Search & Date Filter Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search by customer, account, note, ref...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_transactions_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date Filter Chips (Today, Yesterday, This Week, This Month, All Time)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(DateFilter.values()) { filter ->
                        val isSelected = dateFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { onDateFilterChange(filter) },
                            label = { Text(filter.label, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // 5. Section Header with count
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transactions Ledger (${transactions.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (selectedAccountId != null || dateFilter != DateFilter.ALL || searchQuery.isNotBlank()) {
                    Text(
                        text = "Filtered",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 6. Transactions List
        if (transactions.isEmpty()) {
            item {
                EmptyLedgerPlaceholder(onQuickAdd = { onOpenDialog(TransactionDialogType.CashOut) })
            }
        } else {
            items(transactions, key = { it.transaction.id }) { item ->
                TransactionCard(
                    item = item,
                    formatter = formatter,
                    onDeleteClick = { transactionToDelete = item }
                )
            }
        }
    }

    // Confirm Delete Dialog
    transactionToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction?") },
            text = {
                Text("Are you sure you want to delete this ${item.transaction.type} transaction of Rs. ${formatter.format(item.transaction.amount)}? Account and cash drawer balances will be automatically adjusted.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransaction(item.transaction.id)
                        transactionToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TransactionCard(
    item: TransactionWithDetails,
    formatter: NumberFormat,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = item.transaction

    val isCashOut = t.type == "CASH_OUT"
    val isCashIn = t.type == "CASH_IN"
    val isIncoming = t.type == "INCOMING"
    val isTransfer = t.type == "TRANSFER"

    val badgeColor = when {
        isCashOut -> CashOutRed
        isCashIn -> CashInGreen
        isTransfer -> SlateSecondary
        else -> MaterialTheme.colorScheme.primary
    }

    val badgeText = when (t.type) {
        "CASH_OUT" -> "Cash Out (-)"
        "CASH_IN" -> "Cash In (+)"
        "INCOMING" -> "Incoming (+)"
        "OUTGOING" -> "Outgoing (-)"
        "TRANSFER" -> "Transfer"
        else -> t.type
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("transaction_card_${t.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Row 1: Type badge, Account name, Date & Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Type Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Account Tag
                    Text(
                        text = if (isTransfer && item.toAccountName != null) {
                            "${item.accountName} → ${item.toAccountName}"
                        } else {
                            item.accountName
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${t.dateString} ${t.timeString}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Financial columns (Cash Impact, Account Impact, and Dedicated Fees column)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Cash Drawer impact (negative on cash out, positive on cash in)
                Column {
                    Text(
                        text = "Cash Drawer",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val cashText = when {
                        t.cashImpact > 0 -> "+Rs. ${formatter.format(t.cashImpact)}"
                        t.cashImpact < 0 -> "-Rs. ${formatter.format(Math.abs(t.cashImpact))}"
                        else -> "Rs. 0"
                    }
                    val cashCol = when {
                        t.cashImpact > 0 -> CashInGreen
                        t.cashImpact < 0 -> CashOutRed
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = cashText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = cashCol
                    )
                }

                // Column 2: Account balance impact
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Account Impact",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val accText = when {
                        t.accountImpact > 0 -> "+Rs. ${formatter.format(t.accountImpact)}"
                        t.accountImpact < 0 -> "-Rs. ${formatter.format(Math.abs(t.accountImpact))}"
                        else -> "Rs. ${formatter.format(t.amount)}"
                    }
                    Text(
                        text = accText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Column 3: Dedicated Fee / Commission Column
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Fee (کمیشن)",
                        style = MaterialTheme.typography.labelSmall,
                        color = FeeGold
                    )
                    Text(
                        text = "Rs. ${formatter.format(t.fee)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = FeeGold
                    )
                }
            }

            // Row 3: Customer / Note / Reference ID (if present)
            if (t.customerName.isNotBlank() || t.note.isNotBlank() || t.referenceId.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (t.customerName.isNotBlank()) {
                        Text(
                            text = "Customer: ${t.customerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (t.referenceId.isNotBlank()) {
                        Text(
                            text = "Ref: ${t.referenceId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (t.note.isNotBlank()) {
                    Text(
                        text = "Note: ${t.note}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Row 4: Running Balances
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Acc Bal: Rs. ${formatter.format(item.accountBalanceAfter)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Cash Bal: Rs. ${formatter.format(item.cashBalanceAfter)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyLedgerPlaceholder(onQuickAdd: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "No Transactions Found",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Record incoming transactions, cash out (negative cash), cash in (positive cash) or transfers using the buttons above.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            TextButton(onClick = onQuickAdd) {
                Text("+ Record First Cash Out")
            }
        }
    }
}
